package com.nithyamart.service;

import com.nithyamart.dao.ProductDAO;
import com.nithyamart.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product createProduct(Product product)
            throws SQLException {

        validateProduct(product);

        return productDAO.create(product);
    }

    public Optional<Product> findById(Long productId)
            throws SQLException {

        if (productId == null) {
            return Optional.empty();
        }

        return productDAO.findById(productId);
    }

    public List<Product> findAll()
            throws SQLException {

        return productDAO.findAll();
    }

    public List<Product> search(String keyword,
                                String category)
            throws SQLException {

        return productDAO.search(keyword, category);
    }

    public List<Product> findBySellerId(Long sellerId)
            throws SQLException {

        if (sellerId == null) {
            throw new IllegalArgumentException(
                    "Seller ID is required.");
        }

        return productDAO.findBySellerId(sellerId);
    }

    public boolean updateProduct(Product product,
                                 Long sellerId)
            throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product is required.");
        }

        if (sellerId == null) {
            throw new IllegalArgumentException(
                    "Seller ID is required.");
        }

        product.setSellerId(sellerId);

        validateProduct(product);

        return productDAO.update(product);
    }

    public boolean deleteProduct(Long productId,
                                 Long sellerId)
            throws SQLException {

        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID is required.");
        }

        if (sellerId == null) {
            throw new IllegalArgumentException(
                    "Seller ID is required.");
        }

        return productDAO.delete(productId, sellerId);
    }

    private void validateProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product is required.");
        }

        if (product.getSellerId() == null) {
            throw new IllegalArgumentException(
                    "Seller ID is required.");
        }

        if (product.getName() == null
                || product.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Product name is required.");
        }

        if (product.getPrice() == null
                || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Product price must be zero or greater.");
        }

        if (product.getStockQuantity() == null
                || product.getStockQuantity() < 0) {

            throw new IllegalArgumentException(
                    "Stock quantity must be zero or greater.");
        }

        if (product.getCategory() == null
                || product.getCategory().isBlank()) {

            throw new IllegalArgumentException(
                    "Product category is required.");
        }

        product.setName(product.getName().trim());
        product.setCategory(product.getCategory().trim());

        if (product.getDescription() != null) {
            product.setDescription(
                    product.getDescription().trim());
        }

        if (product.getImageUrl() != null) {
            product.setImageUrl(
                    product.getImageUrl().trim());
        }
    }
}