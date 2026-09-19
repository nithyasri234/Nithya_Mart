package com.nithyamart.model;

import java.math.BigDecimal;

public class SellerOrderItem {

    private Long orderId;
    private Long productId;
    private String productName;
    private Long buyerId;
    private String buyerName;
    private Integer quantity;
    private BigDecimal unitPrice;

    public SellerOrderItem() {
    }

    public SellerOrderItem(Long orderId,
                           Long productId,
                           String productName,
                           Long buyerId,
                           String buyerName,
                           Integer quantity,
                           BigDecimal unitPrice) {

        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}