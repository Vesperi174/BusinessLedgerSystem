package com.businessledger.service;

import com.businessledger.entity.Room;
import com.businessledger.enums.RoomType;
import com.businessledger.service.strategy.FixedPriceStrategy;
import com.businessledger.service.strategy.WeekdayPriceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(new FixedPriceStrategy(), new WeekdayPriceStrategy());
    }

    @Test
    void shouldReturnFixedPriceForSmallRoom() {
        Room room = new Room("1号小包", RoomType.SMALL, new BigDecimal("228.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.now());
        assertEquals(new BigDecimal("228.00"), price);
    }

    @Test
    void shouldReturnFixedPriceForMediumRoom() {
        Room room = new Room("2号中包", RoomType.MEDIUM, new BigDecimal("298.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.now());
        assertEquals(new BigDecimal("298.00"), price);
    }

    @Test
    void shouldReturnFixedPriceForLargeRoom() {
        Room room = new Room("3号大包", RoomType.LARGE, new BigDecimal("398.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.now());
        assertEquals(new BigDecimal("398.00"), price);
    }

    @Test
    void shouldReturnFixedPriceForHallLargeRoom() {
        Room room = new Room("大厅大包", RoomType.HALL_LARGE, new BigDecimal("398.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.now());
        assertEquals(new BigDecimal("398.00"), price);
    }

    @Test
    void shouldReturnWeekdayPriceForHallOnMonday() {
        Room room = new Room("大厅", RoomType.HALL, new BigDecimal("35.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.of(2026, 6, 29));
        assertEquals(new BigDecimal("35.00"), price);
    }

    @Test
    void shouldReturnWeekendPriceForHallOnFriday() {
        Room room = new Room("大厅", RoomType.HALL, new BigDecimal("35.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.of(2026, 7, 3));
        assertEquals(new BigDecimal("45.00"), price);
    }

    @Test
    void shouldReturnWeekendPriceForHallOnSaturday() {
        Room room = new Room("大厅", RoomType.HALL, new BigDecimal("35.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.of(2026, 7, 4));
        assertEquals(new BigDecimal("45.00"), price);
    }

    @Test
    void shouldReturnWeekendPriceForHallOnSunday() {
        Room room = new Room("大厅", RoomType.HALL, new BigDecimal("35.00"));
        BigDecimal price = pricingService.calculatePrice(room, LocalDate.of(2026, 7, 5));
        assertEquals(new BigDecimal("45.00"), price);
    }

    @Test
    void shouldCalculateOvernightFeeForThreePeople() {
        BigDecimal fee = pricingService.calculateOvernightFee(3);
        assertEquals(new BigDecimal("60.00"), fee);
    }

    @Test
    void shouldReturnZeroOvernightFeeForZeroPeople() {
        BigDecimal fee = pricingService.calculateOvernightFee(0);
        assertEquals(BigDecimal.ZERO, fee);
    }
}