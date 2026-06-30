package com.businessledger.event;

import com.businessledger.enums.OrderStatus;

public class OrderStatusChangedEvent {

    private final Long orderId;
    private final Long roomId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Long orderId, Long roomId, OrderStatus oldStatus, OrderStatus newStatus) {
        this.orderId = orderId;
        this.roomId = roomId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }
}