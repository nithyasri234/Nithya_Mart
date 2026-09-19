package com.nithyamart.dao;

import com.nithyamart.model.Review;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private final DataSource dataSource;

    public ReviewDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Review create(Review review) throws SQLException {

        String sql = """
                INSERT INTO reviews
                    (order_id, product_id, buyer_id, rating, comment)
                VALUES
                    (?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, review.getOrderId());
            statement.setLong(2, review.getProductId());
            statement.setLong(3, review.getBuyerId());
            statement.setInt(4, review.getRating());
            statement.setString(5, review.getComment());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    review.setId(keys.getLong(1));
                }
            }
        }

        return findById(review.getId());
    }

    public Review findById(Long id) throws SQLException {

        String sql = """
                SELECT id, order_id, product_id, buyer_id,
                       rating, comment, created_at
                FROM reviews
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapReview(resultSet);
                }
            }
        }

        return null;
    }

    public List<Review> findByProductId(Long productId)
            throws SQLException {

        String sql = """
                SELECT id, order_id, product_id, buyer_id,
                       rating, comment, created_at
                FROM reviews
                WHERE product_id = ?
                ORDER BY created_at DESC
                """;

        List<Review> reviews = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, productId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    reviews.add(mapReview(resultSet));
                }
            }
        }

        return reviews;
    }

    public boolean existsForOrderProduct(Long orderId,
                                         Long productId,
                                         Long buyerId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM reviews
                WHERE order_id = ?
                  AND product_id = ?
                  AND buyer_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, orderId);
            statement.setLong(2, productId);
            statement.setLong(3, buyerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Review mapReview(ResultSet resultSet)
            throws SQLException {

        Timestamp createdTimestamp =
                resultSet.getTimestamp("created_at");

        return new Review(
                resultSet.getLong("id"),
                resultSet.getLong("order_id"),
                resultSet.getLong("product_id"),
                resultSet.getLong("buyer_id"),
                resultSet.getInt("rating"),
                resultSet.getString("comment"),
                createdTimestamp != null
                        ? createdTimestamp.toLocalDateTime()
                        : null
        );
    }
}