package com.nithyamart.controller;

import com.nithyamart.dao.UserDAO;
import com.nithyamart.model.User;
import com.nithyamart.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        DataSource dataSource =
                (DataSource) getServletContext()
                        .getAttribute("dataSource");

        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }

        UserDAO userDAO = new UserDAO(dataSource);
        userService = new UserService(userDAO);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Optional<User> userOptional =
                    userService.login(email, password);

            if (userOptional.isEmpty()) {
                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(
                        "text/plain;charset=UTF-8");

                response.getWriter().print(
                        "Invalid email or password.");

                return;
            }

            User user = userOptional.get();

            /*
             * Create the session and regenerate its ID
             * after successful authentication.
             */
            HttpSession session =
                    request.getSession(true);

            request.changeSessionId();

            session.setMaxInactiveInterval(30 * 60);

            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            response.sendRedirect(
                    request.getContextPath()
                            + "/index.html");

        } catch (IllegalArgumentException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.setContentType(
                    "text/plain;charset=UTF-8");

            response.getWriter().print(
                    e.getMessage());

        } catch (Exception e) {

            getServletContext().log("Login failed", e);

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.setContentType(
                    "text/plain;charset=UTF-8");

            response.getWriter().print(
                    "Unable to login. Please try again later.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}