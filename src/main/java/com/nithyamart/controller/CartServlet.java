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

import com.nithyamart.util.JsonUtil;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/api/v1/cart", "/api/v1/cart/*"})
public class CartServlet extends HttpServlet {

    private CartDAO cartDAO;
    private Gson gson;

    public CartServlet() {
    }

    public CartServlet(CartDAO cartDAO, Gson gson) {
        this.cartDAO = cartDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

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
        gson = JsonUtil.getGson();
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

            CartItemRequest itemReq = extractCartItemRequest(request);
            Long productId = itemReq.productId;
            Integer quantity = itemReq.quantity;

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

            CartItemRequest itemReq = extractCartItemRequest(request);
            Long productId = itemReq.productId;
            Integer quantity = itemReq.quantity;

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
                productId = parseId(request.getPathInfo());
            }

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

    private Long parseId(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        String idText = path.startsWith("/") ? path.substring(1) : path;
        if (idText.endsWith("/")) {
            idText = idText.substring(0, idText.length() - 1);
        }

        try {
            return Long.parseLong(idText.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class CartItemRequest {
        Long productId;
        Integer quantity;
    }

    private CartItemRequest extractCartItemRequest(HttpServletRequest request) {
        Long productId = parseLong(request.getParameter("productId"));
        Integer quantity = parseInteger(request.getParameter("quantity"));

        if (productId != null && quantity != null) {
            CartItemRequest req = new CartItemRequest();
            req.productId = productId;
            req.quantity = quantity;
            return req;
        }

        try {
            String body = request.getReader().lines().collect(java.util.stream.Collectors.joining("\n"));
            if (body != null && !body.isBlank()) {
                body = body.trim();
                if (body.startsWith("{")) {
                    CartItemRequest jsonReq = gson.fromJson(body, CartItemRequest.class);
                    if (jsonReq != null) {
                        if (productId != null) jsonReq.productId = productId;
                        if (quantity != null) jsonReq.quantity = quantity;
                        return jsonReq;
                    }
                } else if (body.contains("=")) {
                    for (String pair : body.split("&")) {
                        String[] parts = pair.split("=", 2);
                        if (parts.length == 2) {
                            String key = java.net.URLDecoder.decode(parts[0], java.nio.charset.StandardCharsets.UTF_8);
                            String val = java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
                            if ("productId".equalsIgnoreCase(key) && productId == null) {
                                productId = parseLong(val);
                            } else if ("quantity".equalsIgnoreCase(key) && quantity == null) {
                                quantity = parseInteger(val);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        CartItemRequest req = new CartItemRequest();
        req.productId = productId;
        req.quantity = quantity;
        return req;
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