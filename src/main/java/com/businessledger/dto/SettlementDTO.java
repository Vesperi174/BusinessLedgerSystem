package com.businessledger.dto;

import java.math.BigDecimal;

public class SettlementDTO {

    private Long orderId;
    private BigDecimal actualPayment;
    private BigDecimal refund;
    private Boolean isMeituan;
    private Integer overnightCount;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getActualPayment() {
        return actualPayment;
    }

    public void setActualPayment(BigDecimal actualPayment) {
        this.actualPayment = actualPayment;
    }

    public BigDecimal getRefund() {
        return refund;
    }

    public void setRefund(BigDecimal refund) {
        this.refund = refund;
    }

    public Boolean getIsMeituan() {
        return isMeituan;
    }

    public void setIsMeituan(Boolean isMeituan) {
        this.isMeituan = isMeituan;
    }

    public Integer getOvernightCount() {
        return overnightCount;
    }

    public void setOvernightCount(Integer overnightCount) {
        this.overnightCount = overnightCount;
    }
}