package com.businessledger.service;

import com.businessledger.dto.OrderCreateDTO;
import com.businessledger.dto.OrderDTO;
import com.businessledger.dto.OrderQueryDTO;
import com.businessledger.dto.SettlementDTO;
import com.businessledger.entity.Order;
import com.businessledger.entity.Room;
import com.businessledger.enums.OrderStatus;
import com.businessledger.enums.RoomStatus;
import com.businessledger.event.OrderStatusChangedEvent;
import com.businessledger.event.RoomStatusChangedEvent;
import com.businessledger.exception.BusinessException;
import com.businessledger.exception.InvalidStatusTransitionException;
import com.businessledger.repository.OrderRepository;
import com.businessledger.util.Constants;
import com.businessledger.util.DateUtil;
import com.businessledger.util.PriceUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private static final Set<OrderStatus> ALLOWED_TRANSITION_FROM_RESERVED = Set.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED);
    private static final Set<OrderStatus> ALLOWED_TRANSITION_FROM_IN_PROGRESS = Set.of(OrderStatus.FINISHED);

    private final OrderRepository orderRepository;
    private final RoomService roomService;
    private final PricingService pricingService;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, RoomService roomService,
                        PricingService pricingService, AuditLogService auditLogService,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.roomService = roomService;
        this.pricingService = pricingService;
        this.auditLogService = auditLogService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createReservation(OrderCreateDTO dto) {
        roomService.validateRoomAvailable(dto.getRoomId());
        Room room = roomService.getRoomById(dto.getRoomId());

        Order order = new Order();
        order.setRoom(room);
        order.setReserveTime(dto.getReserveTime());
        order.setPeopleCount(dto.getPeopleCount() != null ? dto.getPeopleCount() : 0);
        order.setDeposit(dto.getDeposit() != null ? dto.getDeposit() : Constants.DEFAULT_DEPOSIT);
        order.setIsDepositPaid(true);
        order.setActualPayment(Constants.ZERO);
        order.setRefund(Constants.ZERO);
        order.setIsMeituan(false);
        order.setOvernightCount(0);
        order.setOvernightFee(Constants.ZERO);
        order.setStatus(OrderStatus.RESERVED);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        order = orderRepository.save(order);

        roomService.updateRoomStatus(room.getId(), RoomStatus.RESERVED);

        eventPublisher.publishEvent(new RoomStatusChangedEvent(room.getId(), RoomStatus.IDLE, RoomStatus.RESERVED));
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), room.getId(), null, OrderStatus.RESERVED));

        auditLogService.log(order.getId(), "创建预约", String.format("包间[%s] 定金[%s] 人数[%d]",
                room.getName(), order.getDeposit(), order.getPeopleCount()));

        return order;
    }

    @Transactional
    public Order startOrder(Long orderId) {
        Order order = getOrderById(orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.IN_PROGRESS, "开台");

        OrderStatus oldStatus = order.getStatus();
        Room room = order.getRoom();

        order.setStartTime(LocalDateTime.now());
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setUpdateTime(LocalDateTime.now());
        order = orderRepository.save(order);

        roomService.updateRoomStatus(room.getId(), RoomStatus.IN_USE);

        eventPublisher.publishEvent(new RoomStatusChangedEvent(room.getId(), RoomStatus.RESERVED, RoomStatus.IN_USE));
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), room.getId(), oldStatus, OrderStatus.IN_PROGRESS));

        auditLogService.log(order.getId(), "开台", String.format("包间[%s] 从预约开台", room.getName()));

        return order;
    }

    @Transactional
    public Order startWalkIn(Long roomId, int peopleCount) {
        roomService.validateRoomAvailable(roomId);
        Room room = roomService.getRoomById(roomId);

        Order order = new Order();
        order.setRoom(room);
        order.setStartTime(LocalDateTime.now());
        order.setPeopleCount(peopleCount);
        order.setDeposit(Constants.ZERO);
        order.setIsDepositPaid(false);
        order.setActualPayment(Constants.ZERO);
        order.setRefund(Constants.ZERO);
        order.setIsMeituan(false);
        order.setOvernightCount(0);
        order.setOvernightFee(Constants.ZERO);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        order = orderRepository.save(order);

        roomService.updateRoomStatus(room.getId(), RoomStatus.IN_USE);

        eventPublisher.publishEvent(new RoomStatusChangedEvent(room.getId(), RoomStatus.IDLE, RoomStatus.IN_USE));
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), room.getId(), null, OrderStatus.IN_PROGRESS));

        auditLogService.log(order.getId(), "直接开台", String.format("包间[%s] 人数[%d]", room.getName(), peopleCount));

        return order;
    }

    @Transactional
    public Order settle(SettlementDTO dto) {
        Order order = getOrderById(dto.getOrderId());

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new InvalidStatusTransitionException(order.getStatus(), "收款结算");
        }

        Room room = order.getRoom();
        LocalDate today = LocalDate.now();

        BigDecimal roomFee = pricingService.calculatePrice(room, today);

        int overnightCount = dto.getOvernightCount() != null ? dto.getOvernightCount() : 0;
        BigDecimal overnightFee = pricingService.calculateOvernightFee(overnightCount);

        BigDecimal subtotal = PriceUtil.add(roomFee, overnightFee);
        BigDecimal depositReturn = order.getDeposit() != null ? order.getDeposit() : Constants.ZERO;

        order.setOvernightCount(overnightCount);
        order.setOvernightFee(overnightFee);
        order.setActualPayment(dto.getActualPayment() != null ? dto.getActualPayment() : subtotal);
        order.setRefund(dto.getRefund() != null ? dto.getRefund() : Constants.ZERO);
        order.setIsMeituan(dto.getIsMeituan() != null ? dto.getIsMeituan() : false);
        order.setUpdateTime(LocalDateTime.now());

        order = orderRepository.save(order);

        auditLogService.log(order.getId(), "收款结算",
                String.format("包间费[%s] 过夜费[%s] 定金退回[%s] 实付[%s] 退款[%s] 美团[%s]",
                        roomFee, overnightFee, depositReturn,
                        order.getActualPayment(), order.getRefund(),
                        order.getIsMeituan() ? "是" : "否"));

        return order;
    }

    @Transactional
    public Order closeOrder(Long orderId) {
        Order order = getOrderById(orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.FINISHED, "闭台");

        if (order.getActualPayment() == null || order.getActualPayment().compareTo(Constants.ZERO) <= 0) {
            throw new BusinessException("请先完成收款结算后再闭台");
        }

        OrderStatus oldStatus = order.getStatus();
        Room room = order.getRoom();

        order.setEndTime(LocalDateTime.now());
        order.setStatus(OrderStatus.FINISHED);
        order.setUpdateTime(LocalDateTime.now());
        order = orderRepository.save(order);

        roomService.updateRoomStatus(room.getId(), RoomStatus.IDLE);

        eventPublisher.publishEvent(new RoomStatusChangedEvent(room.getId(), RoomStatus.IN_USE, RoomStatus.IDLE));
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), room.getId(), oldStatus, OrderStatus.FINISHED));

        auditLogService.log(order.getId(), "闭台", String.format("包间[%s] 订单结束", room.getName()));

        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED, "标记爽约");

        OrderStatus oldStatus = order.getStatus();
        Room room = order.getRoom();

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdateTime(LocalDateTime.now());
        order = orderRepository.save(order);

        roomService.updateRoomStatus(room.getId(), RoomStatus.IDLE);

        eventPublisher.publishEvent(new RoomStatusChangedEvent(room.getId(), RoomStatus.RESERVED, RoomStatus.IDLE));
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), room.getId(), oldStatus, OrderStatus.CANCELLED));

        auditLogService.log(order.getId(), "标记爽约", String.format("包间[%s] 预约未到，标记为废订单", room.getName()));

        return order;
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderCreateDTO dto) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.FINISHED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("已结束或已取消的订单不可修改");
        }

        Room oldRoom = order.getRoom();

        if (dto.getRoomId() != null && !dto.getRoomId().equals(oldRoom.getId())) {
            roomService.validateRoomAvailable(dto.getRoomId());
            Room newRoom = roomService.getRoomById(dto.getRoomId());
            order.setRoom(newRoom);

            if (order.getStatus() == OrderStatus.RESERVED) {
                roomService.updateRoomStatus(oldRoom.getId(), RoomStatus.IDLE);
                roomService.updateRoomStatus(newRoom.getId(), RoomStatus.RESERVED);
            }
        }

        if (dto.getReserveTime() != null) {
            order.setReserveTime(dto.getReserveTime());
        }
        if (dto.getPeopleCount() != null) {
            order.setPeopleCount(dto.getPeopleCount());
        }
        if (dto.getDeposit() != null) {
            order.setDeposit(dto.getDeposit());
        }

        order.setUpdateTime(LocalDateTime.now());
        order = orderRepository.save(order);

        auditLogService.log(order.getId(), "修改订单", String.format("修改订单信息"));

        return order;
    }

    public List<OrderDTO> queryOrders(OrderQueryDTO dto) {
        List<Order> orders;

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (dto.getStartDate() != null) {
            start = DateUtil.startOfDay(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            end = DateUtil.endOfDay(dto.getEndDate());
        }

        boolean hasRoom = dto.getRoomId() != null;
        boolean hasStatus = dto.getStatus() != null && !dto.getStatus().isBlank();
        boolean hasDateRange = start != null && end != null;

        if (hasRoom && hasStatus && hasDateRange) {
            orders = orderRepository.findByRoomIdAndStatusAndDateRange(
                    dto.getRoomId(), OrderStatus.valueOf(dto.getStatus()), start, end);
        } else if (hasRoom && hasDateRange) {
            orders = orderRepository.findByRoomIdAndDateRange(dto.getRoomId(), start, end);
        } else if (hasStatus && hasDateRange) {
            orders = orderRepository.findByStatusAndDateRange(OrderStatus.valueOf(dto.getStatus()), start, end);
        } else if (hasDateRange) {
            orders = orderRepository.findByDateRange(start, end);
        } else if (hasRoom) {
            orders = orderRepository.findByRoomId(dto.getRoomId());
        } else if (hasStatus) {
            orders = orderRepository.findByStatus(OrderStatus.valueOf(dto.getStatus()));
        } else {
            orders = orderRepository.findAll();
        }

        return toDTOList(orders);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在: " + orderId));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target, String action) {
        Set<OrderStatus> allowed;
        switch (current) {
            case RESERVED:
                allowed = ALLOWED_TRANSITION_FROM_RESERVED;
                break;
            case IN_PROGRESS:
                allowed = ALLOWED_TRANSITION_FROM_IN_PROGRESS;
                break;
            default:
                throw new InvalidStatusTransitionException(current, action);
        }

        if (!allowed.contains(target)) {
            throw new InvalidStatusTransitionException(current, action);
        }
    }

    private List<OrderDTO> toDTOList(List<Order> orders) {
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO dto = new OrderDTO();
            dto.setId(order.getId());
            dto.setRoomName(order.getRoom().getName());
            dto.setStatus(order.getStatus());
            dto.setPeopleCount(order.getPeopleCount());
            dto.setActualPayment(order.getActualPayment());
            dto.setRefund(order.getRefund());
            dto.setIsMeituan(order.getIsMeituan());
            dto.setStartTime(order.getStartTime());
            dto.setEndTime(order.getEndTime());
            dto.setCreateTime(order.getCreateTime());
            dtos.add(dto);
        }
        return dtos;
    }
}