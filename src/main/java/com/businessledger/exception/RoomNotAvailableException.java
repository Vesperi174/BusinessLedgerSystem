package com.businessledger.exception;

public class RoomNotAvailableException extends BusinessException {

    private final Long roomId;

    public RoomNotAvailableException(Long roomId) {
        super("ROOM_NOT_AVAILABLE",
                String.format("包间[%d]当前不可用", roomId));
        this.roomId = roomId;
    }

    public RoomNotAvailableException(Long roomId, String reason) {
        super("ROOM_NOT_AVAILABLE", reason);
        this.roomId = roomId;
    }

    public Long getRoomId() {
        return roomId;
    }
}