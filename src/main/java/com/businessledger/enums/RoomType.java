package com.businessledger.enums;

public enum RoomType {
    SMALL("小包"),
    MEDIUM("中包"),
    LARGE("大包"),
    HALL("大厅"),
    HALL_LARGE("大厅大包");

    private final String description;

    RoomType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}