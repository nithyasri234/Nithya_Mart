package com.nithyamart.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PasswordResetDAO {

    private final DataSource dataSource;

    public PasswordResetDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createToken(Long userId, String token, LocalDateTime expiryTime) throws SQLException {
        // Invalidate any existing active tokens for this user
        invalidateUserTokens(userId);

        String sql = """
                INSERT INTO password_reset_tokens (token, user_id, expiry_time, used)
                VALUES (?, ?, ?, FALSE)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            statement.setLong(2, userId);
            statement.setTimestamp(3, Timestamp.valueOf(expiryTime));
            statement.executeUpdate();
        }
    }

    public Long findValidUserIdByToken(String token) throws SQLException {
        String sql = """
                SELECT user_id, expiry_time, used
                FROM password_reset_tokens
                WHERE token = ? AND used = FALSE
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Timestamp expiry = rs.getTimestamp("expiry_time");
                    if (expiry != null && expiry.toLocalDateTime().isAfter(LocalDateTime.now())) {
                        return rs.getLong("user_id");
                    }
                }
            }
        }
        return null;
    }

    public boolean markTokenUsed(String token) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE token = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            return statement.executeUpdate() > 0;
        }
    }

    public void invalidateUserTokens(Long userId) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE user_id = ? AND used = FALSE";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }
}
