package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.AdminDAO;
import com.nithyamart.model.OrderSummary;
import com.nithyamart.model.Product;
import com.nithyamart.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/admin/*")
public class AdminServlet extends HttpServlet {

    private AdminDAO adminDAO;
    private Gson gson;

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

        adminDAO = new AdminDAO(dataSource);
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        if (!isAdmin(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access is required."
            );
            return;
        }

        try {

            String path =
                    request.getPathInfo();

            if (path == null
                    || path.equals("/")
                    || path.isBlank()) {

                AdminData data =
                        new AdminData(
                                adminDAO.findAllUsers(),
                                adminDAO.findAllOrders(),
                                adminDAO.findAllProducts()
                        );

                writeJson(response, data);
                return;
            }

            switch (path) {

                case "/users":
                    List<User> users =
                            adminDAO.findAllUsers();

                    writeJson(response, users);
                    break;

                case "/orders":
                    List<OrderSummary> orders =
                            adminDAO.findAllOrders();

                    writeJson(response, orders);
                    break;

                case "/products":
                    List<Product> products =
                            adminDAO.findAllProducts();

                    writeJson(response, products);
                    break;

                default:
                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Admin resource not found."
                    );
            }

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to load admin data.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load admin data."
            );
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        if (!isAdmin(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access is required."
            );
            return;
        }

        try {

            String path =
                    request.getPathInfo();

            if (path == null
                    || !path.startsWith("/products/")) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product path."
                );

                return;
            }

            String idText =
                    path.substring("/products/".length());

            Long productId;

            try {
                productId =
                        Long.parseLong(idText);
            } catch (NumberFormatException e) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID."
                );

                return;
            }

            boolean deleted =
                    adminDAO.deleteProduct(productId);

            if (!deleted) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found."
                );

                return;
            }

            writeJson(
                    response,
                    new MessageResponse(
                            "Product removed successfully."
                    )
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to remove product.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to remove product."
            );
        }
    }

    private boolean isAdmin(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        return "ADMIN".equals(
                session.getAttribute("userRole")
        );
    }

    private void writeJson(
            HttpServletResponse response,
            Object data)
            throws IOException {

        response.getWriter()
                .write(gson.toJson(data));
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        writeJson(
                response,
                new MessageResponse(message)
        );
    }

    private static class AdminData {

        private final List<User> users;
        private final List<OrderSummary> orders;
        private final List<Product> products;

        private AdminData(
                List<User> users,
                List<OrderSummary> orders,
                List<Product> products) {

            this.users = users;
            this.orders = orders;
            this.products = products;
        }
    }

    private static class MessageResponse {

        private final String message;

        private MessageResponse(String message) {
            this.message = message;
        }
    }
}