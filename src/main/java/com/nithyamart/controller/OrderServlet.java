package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.OrderDAO;
import com.nithyamart.model.OrderSummary;
import com.nithyamart.model.SellerOrderItem;
import com.nithyamart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/v1/orders", "/api/v1/orders/*"})
public class OrderServlet extends HttpServlet {

    private OrderDAO orderDAO;
    private Gson gson;

    public OrderServlet() {
    }

    public OrderServlet(OrderDAO orderDAO, Gson gson) {
        this.orderDAO = orderDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        orderDAO = new OrderDAO(dataSource);
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
        String role = (String) session.getAttribute("userRole");
        String path = request.getPathInfo();

        try {
            // Specific order detail: GET /api/v1/orders/{id}
            if (path != null && path.length() > 1 && !path.equals("/")) {
                String idStr = path.substring(1);
                if (idStr.contains("/")) {
                    idStr = idStr.substring(0, idStr.indexOf("/"));
                }
                Long orderId = Long.parseLong(idStr);
                OrderSummary order = orderDAO.findOrderById(orderId);

                if (order == null) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found.");
                    return;
                }

                // Verify access permission
                if (!"ADMIN".equals(role) && !order.getBuyerId().equals(userId)) {
                    sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied to this order.");
                    return;
                }

                writeJson(response, order);
                return;
            }

            // Order lists based on role:
            if ("SELLER".equals(role)) {
                List<SellerOrderItem> sellerItems = orderDAO.findOrdersBySellerId(userId);
                writeJson(response, sellerItems);
            } else if ("ADMIN".equals(role)) {
                List<OrderSummary> allOrders = orderDAO.findAllOrders();
                writeJson(response, allOrders);
            } else {
                List<OrderSummary> buyerOrders = orderDAO.findOrdersByBuyerId(userId);
                writeJson(response, buyerOrders);
            }

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID.");
        } catch (Exception e) {
            getServletContext().log("Unable to load orders", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load orders.");
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
        String role = (String) session.getAttribute("userRole");
        String path = request.getPathInfo();

        try {
            // Cancel order: PUT /api/v1/orders/{id}/cancel
            if (path != null && path.endsWith("/cancel")) {
                String idStr = path.substring(1, path.indexOf("/cancel"));
                Long orderId = Long.parseLong(idStr);

                OrderSummary order = orderDAO.findOrderById(orderId);
                if (order == null) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found.");
                    return;
                }

                if (!"ADMIN".equals(role) && !order.getBuyerId().equals(userId)) {
                    sendError(response, HttpServletResponse.SC_FORBIDDEN, "Not authorized to cancel this order.");
                    return;
                }

                if ("DELIVERED".equalsIgnoreCase(order.getOrderStatus()) || "CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Order cannot be cancelled in its current state.");
                    return;
                }

                orderDAO.updateOrderStatus(orderId, "CANCELLED");
                writeJson(response, new MessageResponse("Order has been cancelled successfully."));
                return;
            }

            // Status update: PUT /api/v1/orders/{id}/status
            if (path != null && (path.endsWith("/status") || path.length() > 1)) {
                if (!"ADMIN".equals(role) && !"SELLER".equals(role)) {
                    sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only sellers and admins can update order status.");
                    return;
                }

                String idStr = path.substring(1);
                if (idStr.endsWith("/status")) {
                    idStr = idStr.substring(0, idStr.indexOf("/status"));
                }
                Long orderId = Long.parseLong(idStr);

                String status = request.getParameter("status");
                if (status == null || status.isBlank()) {
                    try {
                        Map<String, String> body = gson.fromJson(request.getReader(), Map.class);
                        if (body != null) {
                            status = body.get("status");
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (status == null || status.isBlank()) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Status parameter is required.");
                    return;
                }

                orderDAO.updateOrderStatus(orderId, status.trim().toUpperCase());
                writeJson(response, new MessageResponse("Order status updated to " + status.trim().toUpperCase()));
                return;
            }

            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order operation.");

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID.");
        } catch (Exception e) {
            getServletContext().log("Unable to update order", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update order.");
        }
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new ErrorResponse(message));
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
