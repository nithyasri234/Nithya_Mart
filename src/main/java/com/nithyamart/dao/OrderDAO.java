package com.nithyamart.dao;

import com.nithyamart.model.OrderSummary;
import com.nithyamart.model.SellerOrderItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private final DataSource dataSource;

    public OrderDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long createOrder(Connection connection,
                            Long buyerId,
                            BigDecimal totalAmount)
            throws SQLException {

        String sql = """
                INSERT INTO orders
                    (buyer_id, total_amount)
                VALUES
                    (?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, buyerId);
            statement.setBigDecimal(2, totalAmount);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("Unable to create order.");
    }

    public void addOrderItem(Connection connection,
                             Long orderId,
                             Long productId,
                             int quantity,
                             BigDecimal unitPrice)
            throws SQLException {

        String sql = """
                INSERT INTO order_items
                    (order_id, product_id, quantity, unit_price)
                VALUES
                    (?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);
            statement.setBigDecimal(4, unitPrice);

            statement.executeUpdate();
        }
    }

    public List<OrderSummary> findOrdersByBuyerId(Long buyerId)
            throws SQLException {

        String sql = """
                SELECT
                    o.id,
                    o.buyer_id,
                    u.name AS buyer_name,
                    o.total_amount,
                    o.created_at
                FROM orders o
                JOIN users u
                    ON o.buyer_id = u.id
                WHERE o.buyer_id = ?
                ORDER BY o.created_at DESC
                """;

        List<OrderSummary> orders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Timestamp createdTimestamp =
                            resultSet.getTimestamp("created_at");

                    orders.add(new OrderSummary(
                            resultSet.getLong("id"),
                            resultSet.getLong("buyer_id"),
                            resultSet.getString("buyer_name"),
                            resultSet.getBigDecimal("total_amount"),
                            createdTimestamp != null
                                    ? createdTimestamp.toLocalDateTime()
                                    : null
                    ));
                }
            }
        }

        return orders;
    }

    public List<SellerOrderItem> findOrdersBySellerId(Long sellerId)
            throws SQLException {

        String sql = """
                SELECT
                    oi.order_id,
                    oi.product_id,
                    p.name AS product_name,
                    o.buyer_id,
                    u.name AS buyer_name,
                    oi.quantity,
                    oi.unit_price
                FROM order_items oi
                JOIN orders o
                    ON oi.order_id = o.id
                JOIN products p
                    ON oi.product_id = p.id
                JOIN users u
                    ON o.buyer_id = u.id
                WHERE p.seller_id = ?
                ORDER BY o.created_at DESC
                """;

        List<SellerOrderItem> items = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, sellerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    items.add(new SellerOrderItem(
                            resultSet.getLong("order_id"),
                            resultSet.getLong("product_id"),
                            resultSet.getString("product_name"),
                            resultSet.getLong("buyer_id"),
                            resultSet.getString("buyer_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price")
                    ));
                }
            }
        }

        return items;
    }

    public List<OrderSummary> findAllOrders()
            throws SQLException {

        String sql = """
                SELECT
                    o.id,
                    o.buyer_id,
                    u.name AS buyer_name,
                    o.total_amount,
                    o.created_at
                FROM orders o
                JOIN users u
                    ON o.buyer_id = u.id
                ORDER BY o.created_at DESC
                """;

        List<OrderSummary> orders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Timestamp createdTimestamp =
                        resultSet.getTimestamp("created_at");

                orders.add(new OrderSummary(
                        resultSet.getLong("id"),
                        resultSet.getLong("buyer_id"),
                        resultSet.getString("buyer_name"),
                        resultSet.getBigDecimal("total_amount"),
                        createdTimestamp != null
                                ? createdTimestamp.toLocalDateTime()
                                : null
                ));
            }
        }

        return orders;
    }
}