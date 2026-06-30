package com.businessledger.event;

import com.businessledger.enums.RoomStatus;

public class RoomStatusChangedEvent {

    private final Long roomId;
    private final RoomStatus oldStatus;
    private final RoomStatus newStatus;

    public RoomStatusChangedEvent(Long roomId, RoomStatus oldStatus, RoomStatus newStatus) {
        this.roomId = roomId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Long getRoomId() {
        return roomId;
    }

    public RoomStatus getOldStatus() {
        return oldStatus;
    }

    public RoomStatus getNewStatus() {
        return newStatus;
    }
}