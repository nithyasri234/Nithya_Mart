package com.nithyamart.model;

import java.time.LocalDateTime;

public class WishlistItem {

    private Long id;
    private Long buyerId;
    private Long productId;
    private Product product;
    private LocalDateTime createdAt;

    public WishlistItem() {
    }

    public WishlistItem(Long id, Long buyerId, Long productId, Product product, LocalDateTime createdAt) {
        this.id = id;
        this.buyerId = buyerId;
        this.productId = productId;
        this.product = product;
        this.createdAt = createdAt;
    }

    public WishlistItem(Long buyerId, Long productId) {
        this.buyerId = buyerId;
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
