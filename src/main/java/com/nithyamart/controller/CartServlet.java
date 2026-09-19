package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.CartDAO;
import com.nithyamart.dao.CartDAO.CartItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/cart/*")
public class CartServlet extends HttpServlet {

    private CartDAO cartDAO;
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
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        try {

            Long buyerId = getBuyerId(request);

            if (buyerId == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Buyer login is required."
                );
                return;
            }

            List<CartItem> items =
                    cartDAO.findByBuyerId(buyerId);

            writeJson(response, items);

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to load cart.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load cart."
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        try {

            Long buyerId = getBuyerId(request);

            if (buyerId == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Buyer login is required."
                );
                return;
            }

            Long productId =
                    parseLong(request.getParameter("productId"));

            Integer quantity =
                    parseInteger(request.getParameter("quantity"));

            if (productId == null || quantity == null
                    || quantity <= 0) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Valid product ID and quantity are required."
                );

                return;
            }

            cartDAO.addItem(
                    buyerId,
                    productId,
                    quantity
            );

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            writeJson(
                    response,
                    new MessageResponse(
                            "Product added to cart."
                    )
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to add item to cart.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to add item to cart."
            );
        }
    }

    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        try {

            Long buyerId = getBuyerId(request);

            if (buyerId == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Buyer login is required."
                );
                return;
            }

            Long productId =
                    parseLong(request.getParameter("productId"));

            Integer quantity =
                    parseInteger(request.getParameter("quantity"));

            if (productId == null || quantity == null
                    || quantity <= 0) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Valid product ID and quantity are required."
                );

                return;
            }

            cartDAO.updateQuantity(
                    buyerId,
                    productId,
                    quantity
            );

            writeJson(
                    response,
                    new MessageResponse(
                            "Cart quantity updated."
                    )
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to update cart.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update cart."
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

        try {

            Long buyerId = getBuyerId(request);

            if (buyerId == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Buyer login is required."
                );
                return;
            }

            Long productId =
                    parseLong(request.getParameter("productId"));

            if (productId == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Valid product ID is required."
                );

                return;
            }

            cartDAO.removeItem(
                    buyerId,
                    productId
            );

            writeJson(
                    response,
                    new MessageResponse(
                            "Product removed from cart."
                    )
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to remove cart item.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to remove cart item."
            );
        }
    }

    private Long getBuyerId(HttpServletRequest request) {

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

    private Long parseLong(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response,
                           Object data)
            throws IOException {

        response.getWriter()
                .write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response,
                           int status,
                           String message)
            throws IOException {

        response.setStatus(status);

        writeJson(
                response,
                new MessageResponse(message)
        );
    }

    private static class MessageResponse {

        private final String message;

        private MessageResponse(String message) {
            this.message = message;
        }
    }
}