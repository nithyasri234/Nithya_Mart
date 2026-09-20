package com.nithyamart.service;

import com.nithyamart.dao.UserDAO;
import com.nithyamart.model.User;
import com.nithyamart.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String name,
                         String email,
                         String password,
                         String role)
            throws SQLException {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters.");
        }

        if (!"BUYER".equals(role) && !"SELLER".equals(role)) {
            throw new IllegalArgumentException(
                    "Role must be BUYER or SELLER.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userDAO.emailExists(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "Email is already registered.");
        }

        String passwordHash =
                PasswordUtil.hashPassword(password);

        User user = new User(
                name.trim(),
                normalizedEmail,
                passwordHash,
                role
        );

        return userDAO.create(user);
    }

    public Optional<User> login(String email,
                                String password)
            throws SQLException {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        User userOptional =
                userDAO.findByEmail(normalizedEmail);

        if (userOptional == null) {
            return null;
        }

        User user = userOptional;

        boolean validPassword =
                PasswordUtil.verifyPassword(
                        password,
                        user.getPasswordHash()
                );

        if (!validPassword) {
            return null;
        }

        return Optional.of(user);
    }

    public Optional<User> findById(Long id)
            throws SQLException {

        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(userDAO.findById(id));
    }
}