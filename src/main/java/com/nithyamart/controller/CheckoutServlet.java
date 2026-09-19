package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.CartDAO;
import com.nithyamart.dao.OrderDAO;
import com.nithyamart.dao.ProductDAO;
import com.nithyamart.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

@WebServlet("/api/v1/checkout")
public class CheckoutServlet extends HttpServlet {

    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
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

        cartDAO = new CartDAO(dataSource);
        productDAO = new ProductDAO(dataSource);
        orderDAO = new OrderDAO(dataSource);
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        Long buyerId = getBuyerId(request);

        if (buyerId == null) {
            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Buyer login is required."
            );
            return;
        }

        try {

            List<CartDAO.CartItem> cartItems =
                    cartDAO.findByBuyerId(buyerId);

            if (cartItems.isEmpty()) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Cart is empty."
                );
                return;
            }

            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CartDAO.CartItem item : cartItems) {

                Product product =
                        item.getProduct();

                if (item.getQuantity()
                        > product.getStockQuantity()) {

                    sendError(
                            response,
                            HttpServletResponse.SC_BAD_REQUEST,
                            "Insufficient stock for product: "
                                    + product.getName()
                    );

                    return;
                }

                BigDecimal itemTotal =
                        product.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                );

                totalAmount =
                        totalAmount.add(itemTotal);
            }

            /*
             * Mock payment confirmation.
             *
             * In the capstone requirements, checkout
             * uses mock payment confirmation instead
             * of a real payment gateway.
             */
            boolean paymentConfirmed =
                    mockPaymentConfirmation(totalAmount);

            if (!paymentConfirmed) {
                sendError(
                        response,
                        HttpServletResponse.SC_PAYMENT_REQUIRED,
                        "Payment confirmation failed."
                );
                return;
            }

            DataSource dataSource =
                    (DataSource) getServletContext()
                            .getAttribute("dataSource");

            try (Connection connection =
                         dataSource.getConnection()) {

                try {

                    connection.setAutoCommit(false);

                    Long orderId =
                            orderDAO.createOrder(
                                    connection,
                                    buyerId,
                                    totalAmount
                            );

                    for (CartDAO.CartItem item : cartItems) {

                        Product product =
                                item.getProduct();

                        orderDAO.addOrderItem(
                                connection,
                                orderId,
                                product.getId(),
                                item.getQuantity(),
                                product.getPrice()
                        );

                        reduceStock(
                                connection,
                                product.getId(),
                                item.getQuantity()
                        );
                    }

                    connection.commit();

                    cartDAO.clearCart(buyerId);

                    response.setStatus(
                            HttpServletResponse.SC_CREATED
                    );

                    writeJson(
                            response,
                            new CheckoutResponse(
                                    orderId,
                                    totalAmount,
                                    "Order placed successfully."
                            )
                    );

                } catch (Exception e) {

                    connection.rollback();

                    throw e;
                }
            }

        } catch (Exception e) {

            getServletContext().log(
                    "Checkout failed.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to complete checkout."
            );
        }
    }

    private boolean mockPaymentConfirmation(
            BigDecimal amount) {

        return amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void reduceStock(Connection connection,
                             Long productId,
                             int quantity)
            throws Exception {

        String sql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE id = ?
                  AND stock_quantity >= ?
                """;

        try (var statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);

            int updated =
                    statement.executeUpdate();

            if (updated == 0) {
                throw new Exception(
                        "Unable to update product stock."
                );
            }
        }
    }

    private Long getBuyerId(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object role =
                session.getAttribute("userRole");

        Object userId =
                session.getAttribute("userId");

        if (!"BUYER".equals(role)
                || !(userId instanceof Long)) {

            return null;
        }

        return (Long) userId;
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
                new ErrorResponse(message)
        );
    }

    private static class ErrorResponse {

        private final String message;

        private ErrorResponse(String message) {
            this.message = message;
        }
    }

    private static class CheckoutResponse {

        private final Long orderId;
        private final BigDecimal totalAmount;
        private final String message;

        private CheckoutResponse(
                Long orderId,
                BigDecimal totalAmount,
                String message) {

            this.orderId = orderId;
            this.totalAmount = totalAmount;
            this.message = message;
        }
    }
}