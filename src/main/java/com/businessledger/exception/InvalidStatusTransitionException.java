package com.businessledger.exception;

import com.businessledger.enums.OrderStatus;

public class InvalidStatusTransitionException extends BusinessException {

    private final OrderStatus currentStatus;
    private final String targetAction;

    public InvalidStatusTransitionException(OrderStatus currentStatus, String targetAction) {
        super("STATUS_TRANSITION_ERROR",
                String.format("当前状态[%s]不允许执行操作[%s]", currentStatus.getDescription(), targetAction));
        this.currentStatus = currentStatus;
        this.targetAction = targetAction;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getTargetAction() {
        return targetAction;
    }
}