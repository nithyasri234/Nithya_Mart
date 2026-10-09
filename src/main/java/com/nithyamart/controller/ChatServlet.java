package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.OrderDAO;
import com.nithyamart.dao.ProductDAO;
import com.nithyamart.model.OrderSummary;
import com.nithyamart.model.Product;
import com.nithyamart.util.ChatProvider;
import com.nithyamart.util.JsonUtil;
import com.nithyamart.util.MockChatProvider;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {

    private ChatProvider chatProvider;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private Gson gson;

    public ChatServlet() {
    }

    public ChatServlet(ChatProvider chatProvider, Gson gson) {
        this.chatProvider = chatProvider != null ? chatProvider : new MockChatProvider();
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    public ChatServlet(ProductDAO productDAO, OrderDAO orderDAO, Gson gson) {
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource != null) {
            this.productDAO = new ProductDAO(dataSource);
            this.orderDAO = new OrderDAO(dataSource);
        }
        this.chatProvider = new MockChatProvider();
        this.gson = JsonUtil.getGson();
    }

    private String getGeminiApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key != null && !key.isBlank()) return key.trim();
        return System.getProperty("gemini.api.key");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        ChatRequest chatRequest = gson.fromJson(request.getReader(), ChatRequest.class);
        if (chatRequest == null || chatRequest.message == null || chatRequest.message.isBlank()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Message is required.");
            return;
        }

        String userMsg = chatRequest.message.trim();
        HttpSession session = request.getSession(false);
        Long buyerId = (session != null && session.getAttribute("userId") instanceof Long) ? (Long) session.getAttribute("userId") : null;

        try {
            String geminiApiKey = getGeminiApiKey();
            boolean hasGemini = geminiApiKey != null && !geminiApiKey.isBlank();

            ChatResult result = processChat(userMsg, buyerId, hasGemini, geminiApiKey);
            writeJson(response, result);

        } catch (Exception e) {
            getServletContext().log("Chat request failed.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process chat request.");
        }
    }

    private ChatResult processChat(String message, Long buyerId, boolean hasGemini, String geminiApiKey) {
        String lower = message.toLowerCase();
        List<String> defaultChips = List.of(
                "Track my order",
                "Show electronics under ₹2000",
                "What is your return policy?",
                "Recommend best fashion",
                "Help with payment methods"
        );

        // 1. Order Tracking queries
        if (lower.contains("order") || lower.contains("track") || lower.contains("status") || lower.contains("delivery")) {
            // Check for specific order number or ID
            Matcher matcher = Pattern.compile("nm-[0-9]{8}-[0-9]{4}", Pattern.CASE_INSENSITIVE).matcher(message);
            String orderNum = matcher.find() ? matcher.group() : null;

            if (orderDAO != null) {
                try {
                    if (orderNum != null) {
                        List<OrderSummary> allOrders = orderDAO.findAllOrders();
                        for (OrderSummary os : allOrders) {
                            if (orderNum.equalsIgnoreCase(os.getOrderNumber())) {
                                String reply = String.format("Order **%s** is currently **%s** (Payment: %s). Total: ₹%s. Delivery to: %s.",
                                        os.getOrderNumber(), os.getOrderStatus(), os.getPaymentStatus(), os.getTotalAmount(), os.getDeliveryAddress());
                                return new ChatResult(reply, Collections.emptyList(), hasGemini, defaultChips);
                            }
                        }
                    }

                    if (buyerId != null) {
                        List<OrderSummary> buyerOrders = orderDAO.findOrdersByBuyerId(buyerId);
                        if (!buyerOrders.isEmpty()) {
                            OrderSummary latest = buyerOrders.get(0);
                            String reply = String.format("Here is your most recent order **%s**:\nStatus: **%s** | Payment: **%s**\nTotal: ₹%s\nDelivery to: %s\nYou can view full details in your [My Orders](orders.html) page.",
                                    latest.getOrderNumber(), latest.getOrderStatus(), latest.getPaymentStatus(), latest.getTotalAmount(), latest.getDeliveryAddress());
                            return new ChatResult(reply, Collections.emptyList(), hasGemini, defaultChips);
                        } else {
                            return new ChatResult("You don't have any placed orders yet. You can browse our products and place an order anytime!", Collections.emptyList(), hasGemini, defaultChips);
                        }
                    } else if (orderNum == null) {
                        return new ChatResult("Please log in to see your active orders, or provide your Order Number (e.g., `NM-20261009-1234`) to check its delivery status.", Collections.emptyList(), hasGemini, defaultChips);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Policy Queries
        if (lower.contains("return") || lower.contains("refund")) {
            String policy = "📦 **Return & Refund Policy:**\n" +
                    "- We offer a 7-day hassle-free return window for eligible products.\n" +
                    "- Items must be unused, in original condition with tags and packaging intact.\n" +
                    "- Refunds are processed back to your original payment method within 3-5 business days after pickup verification.";
            return new ChatResult(policy, Collections.emptyList(), hasGemini, defaultChips);
        }

        if (lower.contains("payment") || lower.contains("pay") || lower.contains("cod") || lower.contains("upi")) {
            String payments = "💳 **Payment Options at Nithya Mart:**\n" +
                    "- **Cash on Delivery (COD)**: Pay when your package arrives at your doorstep.\n" +
                    "- **UPI**: Instant payment via Google Pay, PhonePe, Paytm, or BHIM.\n" +
                    "- **Credit / Debit Cards**: Visa, MasterCard, RuPay, Maestro.\n" +
                    "- **Net Banking**: Supported across all major Indian banks.";
            return new ChatResult(payments, Collections.emptyList(), hasGemini, defaultChips);
        }

        // 3. Product Queries & Recommendations
        BigDecimal maxPrice = null;
        Matcher priceMatcher = Pattern.compile("under\\s*(?:rs\\.?|inr|₹)?\\s*([0-9]+)", Pattern.CASE_INSENSITIVE).matcher(message);
        if (priceMatcher.find()) {
            try {
                maxPrice = new BigDecimal(priceMatcher.group(1));
            } catch (Exception ignored) {}
        }

        String searchKeyword = null;
        String category = null;

        if (lower.contains("electronic") || lower.contains("phone") || lower.contains("headphone") || lower.contains("watch") || lower.contains("laptop")) {
            category = "Electronics";
        } else if (lower.contains("fashion") || lower.contains("shoe") || lower.contains("shirt") || lower.contains("dress") || lower.contains("jacket") || lower.contains("cloth")) {
            category = "Fashion";
        } else if (lower.contains("book") || lower.contains("read")) {
            category = "Books";
        } else if (lower.contains("kitchen") || lower.contains("home") || lower.contains("cookware")) {
            category = "Home & Kitchen";
        } else if (lower.contains("beauty") || lower.contains("skincare") || lower.contains("makeup")) {
            category = "Beauty";
        } else if (lower.contains("grocery") || lower.contains("food") || lower.contains("snack")) {
            category = "Grocery";
        }

        if (category == null && !lower.contains("hello") && !lower.contains("hi") && !lower.contains("help")) {
            searchKeyword = message.replaceAll("(?i)(show|recommend|find|suggest|search|me|some|the|best|buy|please)", "").trim();
        }

        List<Product> matchedProducts = new ArrayList<>();
        if (productDAO != null && (category != null || searchKeyword != null || maxPrice != null)) {
            try {
                matchedProducts = productDAO.search(searchKeyword, category, "price_asc", null, maxPrice);
                if (matchedProducts.size() > 4) {
                    matchedProducts = matchedProducts.subList(0, 4);
                }
            } catch (Exception ignored) {}
        }

        if (!matchedProducts.isEmpty()) {
            StringBuilder reply = new StringBuilder("Here are great recommendations from Nithya Mart's real inventory");
            if (category != null) reply.append(" in **").append(category).append("**");
            if (maxPrice != null) reply.append(" under ₹").append(maxPrice);
            reply.append(":\n\n");
            for (Product p : matchedProducts) {
                reply.append(String.format("• **%s** — ₹%s (%s)\n", p.getName(), p.getPrice(), p.getCategory()));
            }
            reply.append("\nYou can view and purchase any product directly below!");

            return new ChatResult(reply.toString(), matchedProducts, hasGemini, defaultChips);
        }

        // 4. If Gemini API key is configured, call Gemini for advanced conversational reasoning
        if (hasGemini) {
            String geminiReply = callGeminiApi(message, geminiApiKey);
            if (geminiReply != null && !geminiReply.isBlank()) {
                return new ChatResult(geminiReply, Collections.emptyList(), true, defaultChips);
            }
        }

        // 5. Fallback Response explaining state truthfully
        String notice = hasGemini
                ? ""
                : "\n\n*(Note: Gemini generative AI is currently disabled. Set `GEMINI_API_KEY` in Render environment variables for conversational LLM features.)*";

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return new ChatResult("Hello! Welcome to Nithya Mart. I'm your AI shopping assistant. How can I help you today? You can ask me to track orders, recommend products, or check our policies." + notice,
                    Collections.emptyList(), hasGemini, defaultChips);
        }

        return new ChatResult("I can help you browse real products across Electronics, Fashion, Books, and Home & Kitchen, check your order status, or explain our delivery and return policies. Try clicking one of the suggestions below!" + notice,
                Collections.emptyList(), hasGemini, defaultChips);
    }

    private String callGeminiApi(String userPrompt, String apiKey) {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            String systemContext = "You are the friendly, helpful AI shopping assistant for 'Nithya Mart', a premier Indian e-commerce platform with products across Electronics, Fashion, Books, and Home & Kitchen. Prices are in Indian Rupees (₹). Offer polite, concise shopping assistance.";
            Map<String, Object> payload = Map.of(
                    "contents", List.of(
                            Map.of("role", "user", "parts", List.of(Map.of("text", systemContext + "\nUser question: " + userPrompt)))
                    )
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(payload).getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    Map<?, ?> resMap = gson.fromJson(sb.toString(), Map.class);
                    List<?> candidates = (List<?>) resMap.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                        Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                        if (content != null) {
                            List<?> parts = (List<?>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                Map<?, ?> part = (Map<?, ?>) parts.get(0);
                                return (String) part.get("text");
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, Map.of("error", message));
    }

    private static class ChatRequest {
        private String message;
    }

    public static class ChatResult {
        private final String reply;
        private final List<Product> products;
        private final boolean isGeminiEnabled;
        private final List<String> chips;

        public ChatResult(String reply, List<Product> products, boolean isGeminiEnabled, List<String> chips) {
            this.reply = reply;
            this.products = products != null ? products : Collections.emptyList();
            this.isGeminiEnabled = isGeminiEnabled;
            this.chips = chips;
        }

        public String getReply() { return reply; }
        public List<Product> getProducts() { return products; }
        public boolean isGeminiEnabled() { return isGeminiEnabled; }
        public List<String> getChips() { return chips; }
    }
}