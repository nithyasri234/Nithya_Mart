package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.PasswordResetDAO;
import com.nithyamart.dao.UserDAO;
import com.nithyamart.model.User;
import com.nithyamart.util.JsonUtil;
import com.nithyamart.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = {"/api/v1/auth/forgot-password", "/api/v1/auth/reset-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private UserDAO userDAO;
    private PasswordResetDAO passwordResetDAO;
    private Gson gson;

    public ForgotPasswordServlet() {
    }

    public ForgotPasswordServlet(UserDAO userDAO, PasswordResetDAO passwordResetDAO, Gson gson) {
        this.userDAO = userDAO;
        this.passwordResetDAO = passwordResetDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        userDAO = new UserDAO(dataSource);
        passwordResetDAO = new PasswordResetDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String uri = request.getRequestURI();
        if (uri.endsWith("forgot-password")) {
            handleForgotPassword(request, response);
        } else if (uri.endsWith("reset-password")) {
            handleResetPassword(request, response);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found.");
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        if (email == null || email.isBlank()) {
            try {
                Map<String, String> body = gson.fromJson(request.getReader(), Map.class);
                if (body != null) {
                    email = body.get("email");
                }
            } catch (Exception ignored) {
            }
        }

        if (email == null || email.isBlank() || !email.contains("@")) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "A valid email address is required.");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String generatedToken = null;

        try {
            User user = userDAO.findByEmail(normalizedEmail);
            if (user != null) {
                generatedToken = UUID.randomUUID().toString().replace("-", "");
                LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(30);
                passwordResetDAO.createToken(user.getId(), generatedToken, expiryTime);
            }

            // Always return a consistent success response to avoid email enumeration
            String resetUrl = generatedToken != null ? "reset-password.html?token=" + generatedToken : null;
            writeJson(response, new ForgotPasswordResponse(
                    "If an account with that email exists, password reset instructions have been generated.",
                    resetUrl
            ));

        } catch (Exception e) {
            getServletContext().log("Unable to process forgot password request", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process password reset request.");
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");

        if (token == null || newPassword == null) {
            try {
                Map<String, String> body = gson.fromJson(request.getReader(), Map.class);
                if (body != null) {
                    if (token == null) token = body.get("token");
                    if (newPassword == null) newPassword = body.get("newPassword");
                }
            } catch (Exception ignored) {
            }
        }

        if (token == null || token.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Password reset token is required.");
            return;
        }

        if (newPassword == null || newPassword.length() < 6) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "New password must contain at least 6 characters.");
            return;
        }

        try {
            Long userId = passwordResetDAO.findValidUserIdByToken(token.trim());
            if (userId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Reset token is invalid, expired, or has already been used.");
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(newPassword);
            userDAO.updatePassword(userId, passwordHash);
            passwordResetDAO.markTokenUsed(token.trim());

            writeJson(response, new MessageResponse("Password has been reset successfully. You can now log in with your new password."));

        } catch (Exception e) {
            getServletContext().log("Unable to complete password reset", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to reset password.");
        }
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new ErrorResponse(message));
    }

    private static class ForgotPasswordResponse {
        private final String message;
        private final String resetUrl;

        public ForgotPasswordResponse(String message, String resetUrl) {
            this.message = message;
            this.resetUrl = resetUrl;
        }
    }

    private static class MessageResponse {
        private final String message;

        public MessageResponse(String message) {
            this.message = message;
        }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
