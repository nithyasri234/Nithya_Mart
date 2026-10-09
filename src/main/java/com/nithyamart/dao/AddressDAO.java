package com.nithyamart.dao;

import com.nithyamart.model.Address;

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

public class AddressDAO {

    private final DataSource dataSource;

    public AddressDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Address create(Address address) throws SQLException {
        String sql = """
                INSERT INTO addresses
                    (user_id, full_name, phone, address_line, city, state, postal_code, is_default)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection()) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                clearDefault(connection, address.getUserId());
            }

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, address.getUserId());
                statement.setString(2, address.getFullName());
                statement.setString(3, address.getPhone());
                statement.setString(4, address.getAddressLine());
                statement.setString(5, address.getCity());
                statement.setString(6, address.getState());
                statement.setString(7, address.getPostalCode());
                statement.setBoolean(8, address.getIsDefault());

                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        address.setId(keys.getLong(1));
                    }
                }
            }
        }

        return findById(address.getId(), address.getUserId()).orElse(address);
    }

    public Optional<Address> findById(Long id, Long userId) throws SQLException {
        String sql = """
                SELECT id, user_id, full_name, phone, address_line, city, state, postal_code, is_default, created_at
                FROM addresses
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAddress(rs));
                }
            }
        }

        return Optional.empty();
    }

    public List<Address> findByUserId(Long userId) throws SQLException {
        String sql = """
                SELECT id, user_id, full_name, phone, address_line, city, state, postal_code, is_default, created_at
                FROM addresses
                WHERE user_id = ?
                ORDER BY is_default DESC, created_at DESC
                """;

        List<Address> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAddress(rs));
                }
            }
        }

        return list;
    }

    public boolean update(Address address) throws SQLException {
        String sql = """
                UPDATE addresses
                SET full_name = ?,
                    phone = ?,
                    address_line = ?,
                    city = ?,
                    state = ?,
                    postal_code = ?,
                    is_default = ?
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection = dataSource.getConnection()) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                clearDefault(connection, address.getUserId());
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, address.getFullName());
                statement.setString(2, address.getPhone());
                statement.setString(3, address.getAddressLine());
                statement.setString(4, address.getCity());
                statement.setString(5, address.getState());
                statement.setString(6, address.getPostalCode());
                statement.setBoolean(7, address.getIsDefault());
                statement.setLong(8, address.getId());
                statement.setLong(9, address.getUserId());

                return statement.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(Long id, Long userId) throws SQLException {
        String sql = """
                DELETE FROM addresses
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean setDefault(Long id, Long userId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            clearDefault(connection, userId);

            String sql = "UPDATE addresses SET is_default = TRUE WHERE id = ? AND user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.setLong(2, userId);
                return statement.executeUpdate() > 0;
            }
        }
    }

    private void clearDefault(Connection connection, Long userId) throws SQLException {
        String sql = "UPDATE addresses SET is_default = FALSE WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private Address mapAddress(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        return new Address(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("address_line"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("postal_code"),
                rs.getBoolean("is_default"),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}
