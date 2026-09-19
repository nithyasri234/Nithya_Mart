package com.nithyamart.controller;

import com.nithyamart.dao.UserDAO;
import com.nithyamart.model.User;
import com.nithyamart.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {

        DataSource dataSource =
                (DataSource) getServletContext()
                        .getAttribute("dataSource");

        if (dataSource == null) {
            throw new ServletException(
                    "DataSource is not available."
            );
        }

        UserDAO userDAO = new UserDAO(dataSource);

        userService = new UserService(userDAO);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");
        String role = request.getParameter("role");

        try {

            if (password == null
                    || !password.equals(confirmPassword)) {

                throw new IllegalArgumentException(
                        "Passwords do not match."
                );
            }

            User user = userService.register(
                    name,
                    email,
                    password,
                    role
            );

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/login.html"
            );

        } catch (IllegalArgumentException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.setContentType("text/html;charset=UTF-8");

            response.getWriter().println(
                    "<h3>Registration failed: "
                            + escapeHtml(e.getMessage())
                            + "</h3>"
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Registration failed",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to register user."
            );
        }
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
