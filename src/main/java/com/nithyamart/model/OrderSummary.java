package com.nithyamart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummary {

    private Long orderId;
    private Long buyerId;
    private String buyerName;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public OrderSummary() {
    }

    public OrderSummary(Long orderId,
                        Long buyerId,
                        String buyerName,
                        BigDecimal totalAmount,
                        LocalDateTime createdAt) {

        this.orderId = orderId;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}