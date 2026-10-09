package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.OrderDAO;
import com.nithyamart.model.OrderSummary;
import com.nithyamart.util.JsonUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/v1/payment/*", "/api/v1/payments/*"})
public class PaymentServlet extends HttpServlet {

    private OrderDAO orderDAO;
    private Gson gson;

    public PaymentServlet() {
    }

    public PaymentServlet(OrderDAO orderDAO, Gson gson) {
        this.orderDAO = orderDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource != null) {
            this.orderDAO = new OrderDAO(dataSource);
        }
        this.gson = JsonUtil.getGson();
    }

    private String getKeyId() {
        String key = System.getenv("RAZORPAY_KEY_ID");
        if (key != null && !key.isBlank()) return key.trim();
        return System.getProperty("razorpay.key.id");
    }

    private String getKeySecret() {
        String secret = System.getenv("RAZORPAY_KEY_SECRET");
        if (secret != null && !secret.isBlank()) return secret.trim();
        return System.getProperty("razorpay.key.secret");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        if (path == null || path.equals("/") || path.endsWith("/config")) {
            handleConfig(response);
            return;
        }

        if (path.endsWith("/status")) {
            handleStatus(request, response);
            return;
        }

        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Payment endpoint not found.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();

        if (path != null && path.endsWith("/create-order")) {
            handleCreateOrder(request, response);
            return;
        }

        if (path != null && path.endsWith("/verify")) {
            handleVerify(request, response);
            return;
        }

        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Payment endpoint not found.");
    }

    private void handleConfig(HttpServletResponse response) throws IOException {
        String keyId = getKeyId();
        boolean isConfigured = keyId != null && !keyId.isBlank() && getKeySecret() != null && !getKeySecret().isBlank();

        Map<String, Object> data = new HashMap<>();
        data.put("configured", isConfigured);
        data.put("keyId", isConfigured ? keyId : null);
        data.put("supportedMethods", List.of("COD", "UPI", "CARD", "NETBANKING", "WALLET"));

        if (isConfigured) {
            data.put("message", "Payment gateway is configured and active.");
        } else {
            data.put("message", "Payment gateway credentials are not configured. To activate live online payments (UPI, Card, Net Banking), configure RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in Render environment variables. Cash on Delivery is currently available.");
        }

        writeJson(response, data);
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderIdStr = request.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "orderId parameter is required.");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr.trim());
            OrderSummary order = orderDAO.findOrderById(orderId);
            if (order == null) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Order not found.");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getOrderId());
            data.put("orderNumber", order.getOrderNumber());
            data.put("paymentStatus", order.getPaymentStatus());
            data.put("orderStatus", order.getOrderStatus());
            data.put("paymentMethod", order.getPaymentMethod());
            data.put("totalAmount", order.getTotalAmount());

            writeJson(response, data);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to fetch payment status.");
        }
    }

    private void handleCreateOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyId = getKeyId();
        String keySecret = getKeySecret();

        if (keyId == null || keySecret == null || keyId.isBlank() || keySecret.isBlank()) {
            response.setStatus(HttpServletResponse.SC_OK);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("configured", false);
            fallback.put("error", "Razorpay gateway credentials are not configured. To enable live online payments, set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in Render environment variables. You can proceed with Cash on Delivery.");
            writeJson(response, fallback);
            return;
        }

        try {
            Map<?, ?> reqBody = gson.fromJson(request.getReader(), Map.class);
            if (reqBody == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is required.");
                return;
            }

            BigDecimal amount = BigDecimal.ZERO;
            if (reqBody.get("amount") != null) {
                amount = new BigDecimal(reqBody.get("amount").toString());
            }

            Long orderId = null;
            if (reqBody.get("orderId") != null) {
                orderId = Long.parseLong(reqBody.get("orderId").toString());
            }

            if (orderId != null && (amount.compareTo(BigDecimal.ZERO) <= 0)) {
                OrderSummary order = orderDAO.findOrderById(orderId);
                if (order != null) {
                    amount = order.getTotalAmount();
                }
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid payment amount.");
                return;
            }

            long amountPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
            String receipt = "rcpt_" + (orderId != null ? orderId : System.currentTimeMillis());

            // Call Razorpay Orders API
            URL url = new URL("https://api.razorpay.com/v1/orders");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String auth = Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + auth);

            Map<String, Object> rzpPayload = new HashMap<>();
            rzpPayload.put("amount", amountPaise);
            rzpPayload.put("currency", "INR");
            rzpPayload.put("receipt", receipt);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(rzpPayload).getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            InputStream is = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            if (responseCode >= 400) {
                sendError(response, HttpServletResponse.SC_BAD_GATEWAY, "Razorpay API error: " + sb);
                return;
            }

            Map<?, ?> rzpRes = gson.fromJson(sb.toString(), Map.class);
            Map<String, Object> out = new HashMap<>();
            out.put("configured", true);
            out.put("keyId", keyId);
            out.put("razorpayOrderId", rzpRes.get("id"));
            out.put("amount", amountPaise);
            out.put("currency", "INR");
            out.put("orderId", orderId);

            writeJson(response, out);

        } catch (Exception e) {
            getServletContext().log("Payment order creation failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create payment order.");
        }
    }

    private void handleVerify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keySecret = getKeySecret();
        if (keySecret == null || keySecret.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Payment gateway is not configured.");
            return;
        }

        try {
            Map<?, ?> reqBody = gson.fromJson(request.getReader(), Map.class);
            if (reqBody == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Payload is required.");
                return;
            }

            String rzpOrderId = (String) reqBody.get("razorpayOrderId");
            String rzpPaymentId = (String) reqBody.get("razorpayPaymentId");
            String rzpSignature = (String) reqBody.get("razorpaySignature");
            Long orderId = null;
            if (reqBody.get("orderId") != null) {
                orderId = Long.parseLong(reqBody.get("orderId").toString());
            }

            if (rzpOrderId == null || rzpPaymentId == null || rzpSignature == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing payment verification parameters.");
                return;
            }

            String data = rzpOrderId + "|" + rzpPaymentId;
            String generatedSignature = calculateHmacSha256(data, keySecret);

            if (!generatedSignature.equalsIgnoreCase(rzpSignature.trim())) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Payment signature verification failed.");
                return;
            }

            // Payment signature verified! Update order status in DB
            if (orderId != null && orderDAO != null) {
                orderDAO.updatePaymentStatus(orderId, "COMPLETED");
                orderDAO.updateOrderStatus(orderId, "CONFIRMED");
            }

            Map<String, Object> res = new HashMap<>();
            res.put("status", "SUCCESS");
            res.put("message", "Payment verified and order confirmed successfully.");
            res.put("orderId", orderId);

            writeJson(response, res);

        } catch (Exception e) {
            getServletContext().log("Payment verification failed", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payment verification error.");
        }
    }

    private String calculateHmacSha256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, Map.of("error", message));
    }
}
