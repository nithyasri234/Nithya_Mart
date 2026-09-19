package com.nithyamart.dao;

import com.nithyamart.model.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    private final DataSource dataSource;

    public CartDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void addItem(Long buyerId, Long productId, int quantity)
            throws SQLException {

        String sql = """
                MERGE INTO cart_items
                    (buyer_id, product_id, quantity)
                KEY (buyer_id, product_id)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);

            statement.executeUpdate();
        }
    }

    public void updateQuantity(Long buyerId,
                               Long productId,
                               int quantity)
            throws SQLException {

        String sql = """
                UPDATE cart_items
                SET quantity = ?
                WHERE buyer_id = ?
                  AND product_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, buyerId);
            statement.setLong(3, productId);

            statement.executeUpdate();
        }
    }

    public void removeItem(Long buyerId, Long productId)
            throws SQLException {

        String sql = """
                DELETE FROM cart_items
                WHERE buyer_id = ?
                  AND product_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);
            statement.setLong(2, productId);

            statement.executeUpdate();
        }
    }

    public List<CartItem> findByBuyerId(Long buyerId)
            throws SQLException {

        String sql = """
                SELECT
                    c.product_id,
                    c.quantity,
                    p.seller_id,
                    p.name,
                    p.description,
                    p.price,
                    p.stock_quantity,
                    p.category,
                    p.image_url,
                    p.created_at
                FROM cart_items c
                JOIN products p
                    ON c.product_id = p.id
                WHERE c.buyer_id = ?
                ORDER BY c.created_at DESC
                """;

        List<CartItem> items = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Product product = new Product(
                            resultSet.getLong("product_id"),
                            resultSet.getLong("seller_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBigDecimal("price"),
                            resultSet.getInt("stock_quantity"),
                            resultSet.getString("category"),
                            resultSet.getString("image_url"),
                            resultSet.getTimestamp("created_at")
                                    .toLocalDateTime()
                    );

                    CartItem item = new CartItem(
                            product,
                            resultSet.getInt("quantity")
                    );

                    items.add(item);
                }
            }
        }

        return items;
    }

    public void clearCart(Long buyerId) throws SQLException {

        String sql = """
                DELETE FROM cart_items
                WHERE buyer_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);

            statement.executeUpdate();
        }
    }

    public static class CartItem {

        private final Product product;
        private final int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}