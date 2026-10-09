package com.nithyamart.dao;

import com.nithyamart.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;


public class UserDAO {

    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a new user in the database.
     *
     * @param user user information to store
     * @return the created user with generated id and createdAt
     * @throws SQLException if the database operation fails
     */
    public User create(User user) throws SQLException {

        String sql = """
                INSERT INTO users
                    (name, email, password_hash, role)
                VALUES
                    (?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }

        User createdUser = findById(user.getId());
        return createdUser != null ? createdUser : user;
    }

    /**
     * Finds a user by email address.
     *
     * @param email user's email address
     * @return matching user, if found
     * @throws SQLException if the database operation fails
     */
    public User findByEmail(String email) throws SQLException {

        String sql = """
                SELECT id, name, email, password_hash, role, phone, created_at
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * Finds a user by database id.
     *
     * @param id user id
     * @return matching user, if found
     * @throws SQLException if the database operation fails
     */
    public User findById(Long id) throws SQLException {

        String sql = """
                SELECT id, name, email, password_hash, role, phone, created_at
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * Updates a user's name and phone number.
     */
    public boolean updateProfile(Long id, String name, String phone) throws SQLException {
        String sql = """
                UPDATE users
                SET name = ?, phone = ?
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, phone);
            statement.setLong(3, id);

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Updates a user's password hash.
     */
    public boolean updatePassword(Long id, String passwordHash) throws SQLException {
        String sql = """
                UPDATE users
                SET password_hash = ?
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setLong(2, id);

            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Returns all registered users ordered by id desc.
     */
    public java.util.List<User> findAll() throws SQLException {
        String sql = """
                SELECT id, name, email, password_hash, role, phone, created_at
                FROM users
                ORDER BY id DESC
                """;

        java.util.List<User> list = new java.util.ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                list.add(mapUser(resultSet));
            }
        }
        return list;
    }

    /**
     * Checks whether an email address already exists.
     *
     * @param email email address to check
     * @return true when the email already exists
     * @throws SQLException if the database operation fails
     */
    public boolean emailExists(String email) throws SQLException {

        String sql = """
                SELECT 1
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {

        Timestamp createdTimestamp = resultSet.getTimestamp("created_at");

        User user = new User(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("role"),
                createdTimestamp != null
                        ? createdTimestamp.toLocalDateTime()
                        : null
        );

        try {
            user.setPhone(resultSet.getString("phone"));
        } catch (SQLException ignored) {
            // column might not be present in old schema/query projections
        }

        return user;
    }
}