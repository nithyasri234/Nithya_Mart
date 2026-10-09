package com.nithyamart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSummary {

    private Long orderId;
    private Long buyerId;
    private String buyerName;
    private BigDecimal totalAmount;
    private String paymentMethod = "COD";
    private String paymentStatus = "PENDING";
    private String orderStatus = "PLACED";
    private String deliveryAddress;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String couponCode;
    private LocalDateTime createdAt;
    private List<OrderItemDetail> items = new ArrayList<>();

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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemDetail> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetail> items) {
        this.items = items;
    }
}