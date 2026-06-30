package com.businessledger.service;

import com.businessledger.dto.OrderCreateDTO;
import com.businessledger.dto.SettlementDTO;
import com.businessledger.entity.Order;
import com.businessledger.entity.Room;
import com.businessledger.enums.OrderStatus;
import com.businessledger.enums.RoomStatus;
import com.businessledger.enums.RoomType;
import com.businessledger.exception.BusinessException;
import com.businessledger.exception.InvalidStatusTransitionException;
import com.businessledger.exception.RoomNotAvailableException;
import com.businessledger.repository.OrderRepository;
import com.businessledger.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RoomService roomService;

    @Mock
    private PricingService pricingService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderService orderService;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, roomService, pricingService, auditLogService, eventPublisher);

        testRoom = new Room("1号小包", RoomType.SMALL, new BigDecimal("228.00"));
        testRoom.setId(1L);
        testRoom.setStatus(RoomStatus.IDLE);
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setRoomId(1L);
        dto.setReserveTime(LocalDateTime.now().plusHours(1));
        dto.setPeopleCount(4);
        dto.setDeposit(new BigDecimal("50.00"));

        when(roomService.getRoomById(1L)).thenReturn(testRoom);
        doNothing().when(roomService).validateRoomAvailable(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        Order order = orderService.createReservation(dto);

        assertNotNull(order);
        assertEquals(OrderStatus.RESERVED, order.getStatus());
        assertEquals(new BigDecimal("50.00"), order.getDeposit());
        assertEquals(4, order.getPeopleCount());
        assertEquals(testRoom, order.getRoom());

        verify(roomService).updateRoomStatus(1L, RoomStatus.RESERVED);
        verify(auditLogService).log(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenCreatingReservationForOccupiedRoom() {
        doThrow(new RoomNotAvailableException(1L)).when(roomService).validateRoomAvailable(1L);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setRoomId(1L);

        assertThrows(RoomNotAvailableException.class, () -> orderService.createReservation(dto));
    }

    @Test
    void shouldStartOrderFromReserved() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.RESERVED);
        order.setDeposit(new BigDecimal("50.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.startOrder(1L);

        assertEquals(OrderStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getStartTime());
        verify(roomService).updateRoomStatus(1L, RoomStatus.IN_USE);
    }

    @Test
    void shouldThrowExceptionWhenStartingNonReservedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.startOrder(1L));
    }

    @Test
    void shouldStartWalkInSuccessfully() {
        when(roomService.getRoomById(1L)).thenReturn(testRoom);
        doNothing().when(roomService).validateRoomAvailable(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        Order order = orderService.startWalkIn(1L, 3);

        assertNotNull(order);
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals(Constants.ZERO, order.getDeposit());
        assertEquals(3, order.getPeopleCount());
        verify(roomService).updateRoomStatus(1L, RoomStatus.IN_USE);
    }

    @Test
    void shouldSettleOrderSuccessfully() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setDeposit(new BigDecimal("50.00"));
        order.setPeopleCount(4);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(pricingService.calculatePrice(any(Room.class), any())).thenReturn(new BigDecimal("228.00"));
        when(pricingService.calculateOvernightFee(2)).thenReturn(new BigDecimal("40.00"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        SettlementDTO dto = new SettlementDTO();
        dto.setOrderId(1L);
        dto.setActualPayment(new BigDecimal("218.00"));
        dto.setRefund(BigDecimal.ZERO);
        dto.setIsMeituan(false);
        dto.setOvernightCount(2);

        Order result = orderService.settle(dto);

        assertEquals(new BigDecimal("218.00"), result.getActualPayment());
        assertEquals(new BigDecimal("40.00"), result.getOvernightFee());
        assertEquals(2, result.getOvernightCount());
    }

    @Test
    void shouldCloseOrderSuccessfully() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setActualPayment(new BigDecimal("228.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.closeOrder(1L);

        assertEquals(OrderStatus.FINISHED, result.getStatus());
        assertNotNull(result.getEndTime());
        verify(roomService).updateRoomStatus(1L, RoomStatus.IDLE);
    }

    @Test
    void shouldThrowExceptionWhenClosingWithoutPayment() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setActualPayment(BigDecimal.ZERO);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.closeOrder(1L));
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.RESERVED);
        order.setDeposit(new BigDecimal("50.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(roomService).updateRoomStatus(1L, RoomStatus.IDLE);
    }

    @Test
    void shouldThrowExceptionWhenCancellingNonReservedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setRoom(testRoom);
        order.setStatus(OrderStatus.IN_PROGRESS);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.cancelOrder(1L));
    }
}