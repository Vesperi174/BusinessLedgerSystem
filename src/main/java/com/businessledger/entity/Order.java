package com.businessledger.entity;

import com.businessledger.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"order\"")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "reserve_time")
    private LocalDateTime reserveTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "people_count")
    private Integer peopleCount;

    @Column(precision = 10, scale = 2)
    private BigDecimal deposit;

    @Column(name = "is_deposit_paid")
    private Boolean isDepositPaid;

    @Column(name = "actual_payment", precision = 10, scale = 2)
    private BigDecimal actualPayment;

    @Column(precision = 10, scale = 2)
    private BigDecimal refund;

    @Column(name = "is_meituan")
    private Boolean isMeituan;

    @Column(name = "overnight_count")
    private Integer overnightCount;

    @Column(name = "overnight_fee", precision = 10, scale = 2)
    private BigDecimal overnightFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    public Order() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(LocalDateTime reserveTime) {
        this.reserveTime = reserveTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public Boolean getIsDepositPaid() {
        return isDepositPaid;
    }

    public void setIsDepositPaid(Boolean isDepositPaid) {
        this.isDepositPaid = isDepositPaid;
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

    public BigDecimal getOvernightFee() {
        return overnightFee;
    }

    public void setOvernightFee(BigDecimal overnightFee) {
        this.overnightFee = overnightFee;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}