package com.nithyamart.dao;

import com.nithyamart.model.OrderItemDetail;
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
        return createOrder(connection, buyerId, totalAmount, "COD", "PENDING", "PLACED", null, BigDecimal.ZERO, null);
    }

    public Long createOrder(Connection connection,
                            Long buyerId,
                            BigDecimal totalAmount,
                            String paymentMethod,
                            String paymentStatus,
                            String orderStatus,
                            String deliveryAddress,
                            BigDecimal discountAmount,
                            String couponCode)
            throws SQLException {

        String sql = """
                INSERT INTO orders
                    (buyer_id, total_amount, payment_method, payment_status, order_status, delivery_address, discount_amount, coupon_code)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, buyerId);
            statement.setBigDecimal(2, totalAmount);
            statement.setString(3, paymentMethod != null ? paymentMethod : "COD");
            statement.setString(4, paymentStatus != null ? paymentStatus : "PENDING");
            statement.setString(5, orderStatus != null ? orderStatus : "PLACED");
            statement.setString(6, deliveryAddress != null ? deliveryAddress : "Standard Delivery Address");
            statement.setBigDecimal(7, discountAmount != null ? discountAmount : BigDecimal.ZERO);
            statement.setString(8, couponCode);

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
                    o.payment_method,
                    o.payment_status,
                    o.order_status,
                    o.delivery_address,
                    o.discount_amount,
                    o.coupon_code,
                    o.created_at
                FROM orders o
                JOIN users u
                    ON o.buyer_id = u.id
                WHERE o.buyer_id = ?
                ORDER BY o.created_at DESC
                """;

        List<OrderSummary> orders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderSummary order = mapOrderSummary(resultSet);
                    order.setItems(findItemsForOrder(connection, order.getOrderId()));
                    orders.add(order);
                }
            }
        }

        return orders;
    }

    public OrderSummary findOrderById(Long orderId) throws SQLException {
        String sql = """
                SELECT
                    o.id,
                    o.buyer_id,
                    u.name AS buyer_name,
                    o.total_amount,
                    o.payment_method,
                    o.payment_status,
                    o.order_status,
                    o.delivery_address,
                    o.discount_amount,
                    o.coupon_code,
                    o.created_at
                FROM orders o
                JOIN users u
                    ON o.buyer_id = u.id
                WHERE o.id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    OrderSummary order = mapOrderSummary(resultSet);
                    order.setItems(findItemsForOrder(connection, order.getOrderId()));
                    return order;
                }
            }
        }
        return null;
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
                    o.payment_method,
                    o.payment_status,
                    o.order_status,
                    o.delivery_address,
                    o.discount_amount,
                    o.coupon_code,
                    o.created_at
                FROM orders o
                JOIN users u
                    ON o.buyer_id = u.id
                ORDER BY o.created_at DESC
                """;

        List<OrderSummary> orders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderSummary order = mapOrderSummary(resultSet);
                    order.setItems(findItemsForOrder(connection, order.getOrderId()));
                    orders.add(order);
                }
            }
        }

        return orders;
    }

    public boolean updateOrderStatus(Long orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET order_status = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setLong(2, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updatePaymentStatus(Long orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET payment_status = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            statement.setLong(2, orderId);
            return statement.executeUpdate() > 0;
        }
    }

    public List<OrderItemDetail> findItemsForOrder(Connection connection, Long orderId) throws SQLException {
        String sql = """
                SELECT
                    oi.product_id,
                    p.name AS product_name,
                    p.image_url,
                    oi.quantity,
                    oi.unit_price
                FROM order_items oi
                LEFT JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
                """;

        List<OrderItemDetail> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                    int qty = rs.getInt("quantity");
                    BigDecimal total = unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(qty)) : BigDecimal.ZERO;
                    items.add(new OrderItemDetail(
                            rs.getLong("product_id"),
                            rs.getString("product_name"),
                            rs.getString("image_url"),
                            qty,
                            unitPrice,
                            total
                    ));
                }
            }
        }
        return items;
    }

    private OrderSummary mapOrderSummary(ResultSet resultSet) throws SQLException {
        Timestamp createdTimestamp = resultSet.getTimestamp("created_at");

        OrderSummary order = new OrderSummary(
                resultSet.getLong("id"),
                resultSet.getLong("buyer_id"),
                resultSet.getString("buyer_name"),
                resultSet.getBigDecimal("total_amount"),
                createdTimestamp != null
                        ? createdTimestamp.toLocalDateTime()
                        : null
        );

        try {
            order.setPaymentMethod(resultSet.getString("payment_method"));
            order.setPaymentStatus(resultSet.getString("payment_status"));
            order.setOrderStatus(resultSet.getString("order_status"));
            order.setDeliveryAddress(resultSet.getString("delivery_address"));
            order.setDiscountAmount(resultSet.getBigDecimal("discount_amount"));
            order.setCouponCode(resultSet.getString("coupon_code"));
        } catch (SQLException ignored) {
            // column backward compatibility
        }

        return order;
    }
}