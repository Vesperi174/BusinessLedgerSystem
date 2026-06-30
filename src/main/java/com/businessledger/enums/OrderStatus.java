package com.businessledger.enums;

public enum OrderStatus {
    RESERVED("预约中"),
    IN_PROGRESS("进行中"),
    FINISHED("已结束"),
    CANCELLED("废订单");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}