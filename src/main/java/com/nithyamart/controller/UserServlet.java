package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.UserDAO;
import com.nithyamart.model.User;
import com.nithyamart.util.JsonUtil;
import com.nithyamart.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/v1/profile", "/api/v1/profile/*"})
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private Gson gson;

    public UserServlet() {
    }

    public UserServlet(UserDAO userDAO, Gson gson) {
        this.userDAO = userDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        userDAO = new UserDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "User profile not found.");
                return;
            }

            // Do not expose password hash in response
            UserProfileResponse profile = new UserProfileResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getPhone(),
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
            );

            writeJson(response, profile);
        } catch (Exception e) {
            getServletContext().log("Unable to fetch user profile", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch user profile.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        String path = request.getPathInfo();

        try {
            // Password change endpoint: PUT /api/v1/profile/password
            if (path != null && path.contains("password")) {
                Map<String, String> body = gson.fromJson(request.getReader(), Map.class);
                String currentPassword = body != null ? body.get("currentPassword") : request.getParameter("currentPassword");
                String newPassword = body != null ? body.get("newPassword") : request.getParameter("newPassword");

                if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.length() < 6) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "New password must be at least 6 characters.");
                    return;
                }

                User user = userDAO.findById(userId);
                if (user == null || !PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Incorrect current password.");
                    return;
                }

                String newHash = PasswordUtil.hashPassword(newPassword);
                userDAO.updatePassword(userId, newHash);
                writeJson(response, new MessageResponse("Password updated successfully."));
                return;
            }

            // Profile info update endpoint: PUT /api/v1/profile
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");

            if (name == null || name.isBlank()) {
                try {
                    Map<String, String> body = gson.fromJson(request.getReader(), Map.class);
                    if (body != null) {
                        name = body.get("name");
                        phone = body.get("phone");
                    }
                } catch (Exception ignored) {
                }
            }

            if (name == null || name.isBlank()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Name cannot be empty.");
                return;
            }

            name = name.trim();
            phone = phone != null ? phone.trim() : null;

            boolean updated = userDAO.updateProfile(userId, name, phone);
            if (!updated) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "User profile could not be updated.");
                return;
            }

            session.setAttribute("userName", name);

            User user = userDAO.findById(userId);
            UserProfileResponse profile = new UserProfileResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getPhone(),
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
            );

            writeJson(response, profile);

        } catch (Exception e) {
            getServletContext().log("Unable to update profile", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update profile.");
        }
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new ErrorResponse(message));
    }

    private static class UserProfileResponse {
        private final Long id;
        private final String name;
        private final String email;
        private final String role;
        private final String phone;
        private final String createdAt;

        public UserProfileResponse(Long id, String name, String email, String role, String phone, String createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.phone = phone;
            this.createdAt = createdAt;
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
