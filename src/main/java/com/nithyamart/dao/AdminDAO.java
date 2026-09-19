package com.nithyamart.dao;

import com.nithyamart.model.OrderSummary;
import com.nithyamart.model.Product;
import com.nithyamart.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    private final DataSource dataSource;

    public AdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<User> findAllUsers() throws SQLException {

        String sql = """
                SELECT id, name, email, password_hash, role, created_at
                FROM users
                ORDER BY created_at DESC
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Timestamp createdTimestamp =
                        resultSet.getTimestamp("created_at");

                users.add(new User(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("role"),
                        createdTimestamp != null
                                ? createdTimestamp.toLocalDateTime()
                                : null
                ));
            }
        }

        return users;
    }

    public List<OrderSummary> findAllOrders() throws SQLException {

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

    public List<Product> findAllProducts() throws SQLException {

        String sql = """
                SELECT
                    id,
                    seller_id,
                    name,
                    description,
                    price,
                    stock_quantity,
                    category,
                    image_url,
                    created_at
                FROM products
                ORDER BY created_at DESC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Timestamp createdTimestamp =
                        resultSet.getTimestamp("created_at");

                products.add(new Product(
                        resultSet.getLong("id"),
                        resultSet.getLong("seller_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getInt("stock_quantity"),
                        resultSet.getString("category"),
                        resultSet.getString("image_url"),
                        createdTimestamp != null
                                ? createdTimestamp.toLocalDateTime()
                                : null
                ));
            }
        }

        return products;
    }

    public boolean deleteProduct(Long productId)
            throws SQLException {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            return statement.executeUpdate() > 0;
        }
    }
}