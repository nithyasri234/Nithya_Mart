package com.nithyamart.dao;

import com.nithyamart.model.Product;
import com.nithyamart.model.WishlistItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    private final DataSource dataSource;

    public WishlistDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<WishlistItem> findByBuyerId(Long buyerId) throws SQLException {
        String sql = """
                SELECT w.id, w.buyer_id, w.product_id, w.created_at,
                       p.id AS p_id, p.seller_id, p.name, p.description,
                       p.price, p.stock_quantity, p.category, p.image_url, p.created_at AS p_created_at
                FROM wishlist_items w
                JOIN products p ON w.product_id = p.id
                WHERE w.buyer_id = ?
                ORDER BY w.created_at DESC
                """;

        List<WishlistItem> items = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Timestamp wTime = rs.getTimestamp("created_at");
                    Timestamp pTime = rs.getTimestamp("p_created_at");

                    Product product = new Product(
                            rs.getLong("p_id"),
                            rs.getLong("seller_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("stock_quantity"),
                            rs.getString("category"),
                            rs.getString("image_url"),
                            pTime != null ? pTime.toLocalDateTime() : null
                    );

                    items.add(new WishlistItem(
                            rs.getLong("id"),
                            rs.getLong("buyer_id"),
                            rs.getLong("product_id"),
                            product,
                            wTime != null ? wTime.toLocalDateTime() : null
                    ));
                }
            }
        }

        return items;
    }

    public boolean addItem(Long buyerId, Long productId) throws SQLException {
        String sql = """
                MERGE INTO wishlist_items (buyer_id, product_id)
                KEY (buyer_id, product_id)
                VALUES (?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean removeItem(Long buyerId, Long productId) throws SQLException {
        String sql = """
                DELETE FROM wishlist_items
                WHERE buyer_id = ? AND product_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean isInWishlist(Long buyerId, Long productId) throws SQLException {
        String sql = """
                SELECT 1 FROM wishlist_items
                WHERE buyer_id = ? AND product_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
