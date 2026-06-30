package com.businessledger.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceUtil {

    private PriceUtil() {
    }

    public static BigDecimal multiply(BigDecimal price, int count) {
        return price.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return nullSafe(a).add(nullSafe(b)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return nullSafe(a).subtract(nullSafe(b)).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}