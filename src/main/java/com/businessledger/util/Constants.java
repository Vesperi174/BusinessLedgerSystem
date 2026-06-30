package com.businessledger.util;

import java.math.BigDecimal;

public class Constants {

    private Constants() {
    }

    public static final BigDecimal DEFAULT_DEPOSIT = new BigDecimal("50.00");

    public static final BigDecimal OVERNIGHT_FEE_PER_PERSON = new BigDecimal("20.00");

    public static final BigDecimal ZERO = BigDecimal.ZERO;

    public static final int DEFAULT_LOG_KEEP_COUNT = 500;

    public static final int MIN_PEOPLE_COUNT = 1;
    public static final int MAX_PEOPLE_COUNT = 999;
}