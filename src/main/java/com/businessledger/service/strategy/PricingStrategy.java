package com.businessledger.service.strategy;

import com.businessledger.entity.Room;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingStrategy {

    BigDecimal calculatePrice(Room room, LocalDate date);
}