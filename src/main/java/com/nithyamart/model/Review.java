package com.nithyamart.model;

import java.time.LocalDateTime;

public class Review {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long buyerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(Long id, Long orderId, Long productId,
                  Long buyerId, Integer rating,
                  String comment, LocalDateTime createdAt) {

        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Review(Long orderId, Long productId,
                  Long buyerId, Integer rating,
                  String comment) {

        this.orderId = orderId;
        this.productId = productId;
        this.buyerId = buyerId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}