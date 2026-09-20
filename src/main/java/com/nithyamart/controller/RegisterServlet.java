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
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserService userService;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {

        DataSource dataSource =
                (DataSource) getServletContext()
                        .getAttribute("dataSource");

        if (dataSource == null) {
            throw new ServletException(
                    "Database connection is not available."
            );
        }

        userDAO = new UserDAO(dataSource);

        userService = new UserService(userDAO);
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name =
                request.getParameter("name");

        String email =
                request.getParameter("email");

        String password =
                request.getParameter("password");

        String confirmPassword =
                request.getParameter("confirmPassword");

        String role =
                request.getParameter("role");


        /* -----------------------------------------
           Basic validation
           ----------------------------------------- */

        if (name == null ||
                email == null ||
                password == null ||
                confirmPassword == null ||
                role == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "All fields are required."
            );

            return;
        }


        if (!password.equals(confirmPassword)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Passwords do not match."
            );

            return;
        }


        role = role.trim().toUpperCase();


        if (!role.equals("BUYER") &&
                !role.equals("SELLER")) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid account type."
            );

            return;
        }


        try {

            /* -----------------------------------------
               Create account
               ----------------------------------------- */

            userService.register(
                    name.trim(),
                    email.trim(),
                    password,
                    role
            );


            /* -----------------------------------------
               Get newly created user
               ----------------------------------------- */
User user;

try {

    user = userDAO.findByEmail(
            email.trim().toLowerCase()
    );

} catch (SQLException e) {

    e.printStackTrace();

    response.sendError(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Account was created but could not be loaded."
    );

    return;
}


if (user == null) {

    response.sendError(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Account was created but could not be loaded."
    );

    return;
}


            /* -----------------------------------------
               Create login session
               ----------------------------------------- */

            HttpSession session =
                    request.getSession(true);

            session.setMaxInactiveInterval(30 * 60);


            /*
             * Regenerate session ID after registration.
             * This prevents session fixation.
             */
            try {
                request.changeSessionId();
            } catch (IllegalStateException ignored) {
                // Session ID regeneration may not be available
                // in some container situations.
            }


            session.setAttribute(
                    "userId",
                    user.getId()
            );

            session.setAttribute(
                    "userName",
                    user.getName()
            );

            session.setAttribute(
                    "userEmail",
                    user.getEmail()
            );

            session.setAttribute(
                    "userRole",
                    user.getRole()
            );


            /* -----------------------------------------
               Redirect according to role
               ----------------------------------------- */

            if ("SELLER".equals(user.getRole())) {

                /*
                 * Seller goes directly to product creation.
                 */
                response.sendRedirect(
                        request.getContextPath()
                                + "/addproduct.html"
                );

            } else {

                /*
                 * Buyer goes directly to shopping page.
                 */
                response.sendRedirect(
                        request.getContextPath()
                                + "/index.html"
                );
            }


        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to create account. Please try again."
            );
        }
    }
}