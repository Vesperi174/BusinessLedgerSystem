package com.businessledger.service.strategy;

import com.businessledger.entity.Room;
import com.businessledger.util.DateUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class WeekdayPriceStrategy implements PricingStrategy {

    private static final BigDecimal WEEKDAY_PRICE = new BigDecimal("35.00");
    private static final BigDecimal WEEKEND_PRICE = new BigDecimal("45.00");

    @Override
    public BigDecimal calculatePrice(Room room, LocalDate date) {
        return DateUtil.isWeekend(date) ? WEEKEND_PRICE : WEEKDAY_PRICE;
    }
}