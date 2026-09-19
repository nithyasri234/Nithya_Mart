package com.nithyamart.dao;

import com.nithyamart.model.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    private final DataSource dataSource;

    public ProductDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Product create(Product product) throws SQLException {

        String sql = """
                INSERT INTO products
                    (seller_id, name, description, price,
                     stock_quantity, category, image_url)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, product.getSellerId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStockQuantity());
            statement.setString(6, product.getCategory());
            statement.setString(7, product.getImageUrl());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getLong(1));
                }
            }
        }

        return findById(product.getId())
                .orElse(product);
    }

    public Optional<Product> findById(Long id) throws SQLException {

        String sql = """
                SELECT id, seller_id, name, description, price,
                       stock_quantity, category, image_url, created_at
                FROM products
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapProduct(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<Product> findAll() throws SQLException {

        String sql = """
                SELECT id, seller_id, name, description, price,
                       stock_quantity, category, image_url, created_at
                FROM products
                ORDER BY created_at DESC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
        }

        return products;
    }

    public List<Product> findBySellerId(Long sellerId)
            throws SQLException {

        String sql = """
                SELECT id, seller_id, name, description, price,
                       stock_quantity, category, image_url, created_at
                FROM products
                WHERE seller_id = ?
                ORDER BY created_at DESC
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, sellerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }

        return products;
    }

    public List<Product> search(String keyword, String category)
            throws SQLException {

        StringBuilder sql = new StringBuilder("""
                SELECT id, seller_id, name, description, price,
                       stock_quantity, category, image_url, created_at
                FROM products
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                    AND (
                        LOWER(name) LIKE ?
                        OR LOWER(description) LIKE ?
                    )
                    """);

            String searchValue = "%" + keyword.toLowerCase() + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
        }

        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            parameters.add(category);
        }

        sql.append(" ORDER BY created_at DESC");

        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }

        return products;
    }

    public boolean update(Product product) throws SQLException {

        String sql = """
                UPDATE products
                SET name = ?,
                    description = ?,
                    price = ?,
                    stock_quantity = ?,
                    category = ?,
                    image_url = ?
                WHERE id = ?
                  AND seller_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setString(5, product.getCategory());
            statement.setString(6, product.getImageUrl());
            statement.setLong(7, product.getId());
            statement.setLong(8, product.getSellerId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(Long productId, Long sellerId)
            throws SQLException {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                  AND seller_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, productId);
            statement.setLong(2, sellerId);

            return statement.executeUpdate() > 0;
        }
    }

    private Product mapProduct(ResultSet resultSet)
            throws SQLException {

        Timestamp createdTimestamp =
                resultSet.getTimestamp("created_at");

        return new Product(
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
        );
    }
}