package com.businessledger.service;

import com.businessledger.entity.Room;
import com.businessledger.enums.RoomType;
import com.businessledger.service.strategy.FixedPriceStrategy;
import com.businessledger.service.strategy.PricingStrategy;
import com.businessledger.service.strategy.WeekdayPriceStrategy;
import com.businessledger.util.Constants;
import com.businessledger.util.PriceUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class PricingService {

    private final Map<RoomType, PricingStrategy> strategyMap;

    public PricingService(FixedPriceStrategy fixedPriceStrategy, WeekdayPriceStrategy weekdayPriceStrategy) {
        this.strategyMap = Map.of(
                RoomType.SMALL, fixedPriceStrategy,
                RoomType.MEDIUM, fixedPriceStrategy,
                RoomType.LARGE, fixedPriceStrategy,
                RoomType.HALL_LARGE, fixedPriceStrategy,
                RoomType.HALL, weekdayPriceStrategy
        );
    }

    public BigDecimal calculatePrice(Room room, LocalDate date) {
        PricingStrategy strategy = strategyMap.get(room.getRoomType());
        if (strategy == null) {
            throw new IllegalArgumentException("未知的包间类型: " + room.getRoomType());
        }
        return strategy.calculatePrice(room, date);
    }

    public BigDecimal calculateOvernightFee(int count) {
        if (count <= 0) {
            return Constants.ZERO;
        }
        return PriceUtil.multiply(Constants.OVERNIGHT_FEE_PER_PERSON, count);
    }
}