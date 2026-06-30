package com.businessledger.service.strategy;

import com.businessledger.entity.Room;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class FixedPriceStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Room room, LocalDate date) {
        return room.getPrice();
    }
}