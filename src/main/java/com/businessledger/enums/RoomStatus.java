package com.businessledger.enums;

public enum RoomStatus {
    IDLE("空闲"),
    RESERVED("预约"),
    IN_USE("使用中");

    private final String description;

    RoomStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}