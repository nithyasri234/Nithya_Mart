package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.AddressDAO;
import com.nithyamart.dao.CartDAO;
import com.nithyamart.dao.CouponDAO;
import com.nithyamart.dao.OrderDAO;
import com.nithyamart.dao.ProductDAO;
import com.nithyamart.model.Address;
import com.nithyamart.model.Coupon;
import com.nithyamart.model.Product;
import com.nithyamart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/api/v1/checkout", "/api/v1/checkout/*"})
public class CheckoutServlet extends HttpServlet {

    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private CouponDAO couponDAO;
    private AddressDAO addressDAO;
    private Gson gson;

    public CheckoutServlet() {
    }

    public CheckoutServlet(CartDAO cartDAO, ProductDAO productDAO, OrderDAO orderDAO, Gson gson) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    public CheckoutServlet(CartDAO cartDAO, ProductDAO productDAO, OrderDAO orderDAO, CouponDAO couponDAO, AddressDAO addressDAO, Gson gson) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.couponDAO = couponDAO;
        this.addressDAO = addressDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }

        cartDAO = new CartDAO(dataSource);
        productDAO = new ProductDAO(dataSource);
        orderDAO = new OrderDAO(dataSource);
        couponDAO = new CouponDAO(dataSource);
        addressDAO = new AddressDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Long buyerId = getBuyerId(request);
        if (buyerId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Buyer login is required.");
            return;
        }

        DataSource dataSource = null;
        try {
            if (getServletContext() != null) {
                dataSource = (DataSource) getServletContext().getAttribute("dataSource");
            }
        } catch (Exception ignored) {}

        if (dataSource != null) {
            if (couponDAO == null) {
                couponDAO = new CouponDAO(dataSource);
            }
            if (addressDAO == null) {
                addressDAO = new AddressDAO(dataSource);
            }
        }

        CheckoutRequest requestData = null;
        try {
            BufferedReader reader = request.getReader();
            if (reader != null) {
                requestData = gson.fromJson(reader, CheckoutRequest.class);
            }
        } catch (Exception ignored) {
        }

        if (requestData == null) {
            requestData = new CheckoutRequest();
        }

        // Support query/form parameter fallbacks
        if (requestData.getProductId() == null && request.getParameter("productId") != null) {
            try {
                requestData.setProductId(Long.parseLong(request.getParameter("productId")));
            } catch (NumberFormatException ignored) {}
        }
        if (requestData.getQuantity() == null && request.getParameter("quantity") != null) {
            try {
                requestData.setQuantity(Integer.parseInt(request.getParameter("quantity")));
            } catch (NumberFormatException ignored) {}
        }
        if (requestData.getDeliveryAddress() == null && request.getParameter("deliveryAddress") != null) {
            requestData.setDeliveryAddress(request.getParameter("deliveryAddress"));
        }
        if (requestData.getPaymentMethod() == null && request.getParameter("paymentMethod") != null) {
            requestData.setPaymentMethod(request.getParameter("paymentMethod"));
        }
        if (requestData.getCouponCode() == null && request.getParameter("couponCode") != null) {
            requestData.setCouponCode(request.getParameter("couponCode"));
        }

        try {
            // 1. Identify items to purchase
            List<ItemToPurchase> items = new ArrayList<>();
            boolean isDirectCheckout = false;

            if (requestData.getProductId() != null && requestData.getProductId() > 0) {
                // Direct Buy Now / Shop Now checkout
                isDirectCheckout = true;
                int quantity = (requestData.getQuantity() != null && requestData.getQuantity() > 0) ? requestData.getQuantity() : 1;
                Product product = productDAO.findById(requestData.getProductId()).orElse(null);
                if (product == null) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Product not found.");
                    return;
                }
                if (quantity > product.getStockQuantity()) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Insufficient stock for product: " + product.getName());
                    return;
                }
                items.add(new ItemToPurchase(product, quantity));
            } else {
                // Cart checkout
                List<CartDAO.CartItem> cartItems = cartDAO.findByBuyerId(buyerId);
                if (cartItems.isEmpty()) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Cart is empty.");
                    return;
                }
                for (CartDAO.CartItem ci : cartItems) {
                    Product product = ci.getProduct();
                    if (ci.getQuantity() > product.getStockQuantity()) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Insufficient stock for product: " + product.getName());
                        return;
                    }
                    items.add(new ItemToPurchase(product, ci.getQuantity()));
                }
            }

            // 2. Resolve Delivery Address snapshot
            String addressSnapshot = resolveDeliveryAddress(buyerId, requestData);

            // 3. Compute subtotal from verified backend product prices
            BigDecimal subtotal = BigDecimal.ZERO;
            for (ItemToPurchase item : items) {
                BigDecimal itemTotal = item.product.getPrice().multiply(BigDecimal.valueOf(item.quantity));
                subtotal = subtotal.add(itemTotal);
            }

            // 4. Validate coupon and calculate discount on backend
            BigDecimal discountAmount = BigDecimal.ZERO;
            String couponCode = requestData.getCouponCode() != null ? requestData.getCouponCode().trim().toUpperCase() : null;
            if (couponCode != null && !couponCode.isBlank() && couponDAO != null) {
                Coupon coupon = couponDAO.findByCode(couponCode);
                if (coupon != null && coupon.isActive()) {
                    discountAmount = couponDAO.calculateDiscount(coupon, subtotal);
                }
            }

            BigDecimal totalAmount = subtotal.subtract(discountAmount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }

            // 5. Payment method and status
            String paymentMethod = requestData.getPaymentMethod();
            if (paymentMethod == null || paymentMethod.isBlank()) {
                paymentMethod = "COD";
            } else {
                paymentMethod = paymentMethod.trim().toUpperCase();
            }

            String paymentStatus = "PENDING";
            String orderStatus = "PLACED";

            if ("ONLINE".equals(paymentMethod) || "UPI".equals(paymentMethod) || "CARD".equals(paymentMethod) || "NETBANKING".equals(paymentMethod)) {
                if (requestData.getPaymentId() != null && !requestData.getPaymentId().isBlank()) {
                    paymentStatus = "COMPLETED";
                    orderStatus = "CONFIRMED";
                } else {
                    paymentStatus = "PENDING";
                    orderStatus = "PLACED";
                }
            }

            // 6. Generate unique Order Number
            LocalDate today = LocalDate.now();
            String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);
            int randSuffix = (int) (1000 + Math.random() * 9000);
            String orderNumber = "NM-" + datePart + "-" + randSuffix;

            // 7. Atomic database transaction
            try (Connection connection = dataSource.getConnection()) {
                try {
                    connection.setAutoCommit(false);

                    Long orderId = orderDAO.createOrder(
                            connection,
                            buyerId,
                            totalAmount,
                            paymentMethod,
                            paymentStatus,
                            orderStatus,
                            addressSnapshot,
                            discountAmount,
                            couponCode,
                            orderNumber
                    );

                    for (ItemToPurchase item : items) {
                        orderDAO.addOrderItem(
                                connection,
                                orderId,
                                item.product.getId(),
                                item.quantity,
                                item.product.getPrice(),
                                item.product.getName(),
                                item.product.getImageUrl()
                        );

                        reduceStock(connection, item.product.getId(), item.quantity);
                    }

                    connection.commit();

                    // Only clear cart if checkout came from cart
                    if (!isDirectCheckout) {
                        cartDAO.clearCart(buyerId);
                    }

                    response.setStatus(HttpServletResponse.SC_CREATED);
                    writeJson(
                            response,
                            new CheckoutResponse(
                                    orderId,
                                    orderNumber,
                                    totalAmount,
                                    discountAmount,
                                    orderStatus,
                                    paymentStatus,
                                    "Order placed successfully."
                            )
                    );

                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                }
            }

        } catch (Exception e) {
            getServletContext().log("Checkout failed.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to complete checkout.");
        }
    }

    private String resolveDeliveryAddress(Long buyerId, CheckoutRequest requestData) {
        if (addressDAO != null && requestData.getAddressId() != null && requestData.getAddressId() > 0) {
            try {
                Optional<Address> opt = addressDAO.findById(requestData.getAddressId(), buyerId);
                if (opt.isPresent()) {
                    Address a = opt.get();
                    return formatAddress(a);
                }
            } catch (Exception ignored) {}
        }

        if (requestData.getDeliveryAddress() != null && !requestData.getDeliveryAddress().isBlank()) {
            return requestData.getDeliveryAddress().trim();
        }

        // Try user default address
        if (addressDAO != null) {
            try {
                List<Address> addresses = addressDAO.findByUserId(buyerId);
                if (!addresses.isEmpty()) {
                    return formatAddress(addresses.get(0));
                }
            } catch (Exception ignored) {}
        }

        return "Standard Delivery Address";
    }

    private String formatAddress(Address a) {
        StringBuilder sb = new StringBuilder();
        if (a.getFullName() != null) sb.append(a.getFullName());
        if (a.getAddressLine() != null) sb.append(", ").append(a.getAddressLine());
        if (a.getCity() != null) sb.append(", ").append(a.getCity());
        if (a.getState() != null) sb.append(", ").append(a.getState());
        if (a.getPostalCode() != null) sb.append(" - ").append(a.getPostalCode());
        if (a.getPhone() != null) sb.append(", Mobile: ").append(a.getPhone());
        return sb.toString();
    }

    private void reduceStock(Connection connection, Long productId, int quantity) throws Exception {
        String sql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE id = ?
                  AND stock_quantity >= ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);

            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new Exception("Unable to update product stock.");
            }
        }
    }

    private Long getBuyerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object role = session.getAttribute("userRole");
        Object userId = session.getAttribute("userId");

        if (!"BUYER".equals(role) || !(userId instanceof Long)) {
            return null;
        }

        return (Long) userId;
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new ErrorResponse(message));
    }

    private static class ItemToPurchase {
        private final Product product;
        private final int quantity;

        public ItemToPurchase(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    public static class CheckoutRequest {
        private Long productId;
        private Integer quantity;
        private Long addressId;
        private String deliveryAddress;
        private String paymentMethod;
        private String paymentId;
        private String couponCode;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Long getAddressId() { return addressId; }
        public void setAddressId(Long addressId) { this.addressId = addressId; }

        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    }

    public static class CheckoutResponse {
        private final Long orderId;
        private final String orderNumber;
        private final BigDecimal totalAmount;
        private final BigDecimal discountAmount;
        private final String orderStatus;
        private final String paymentStatus;
        private final String message;

        public CheckoutResponse(Long orderId, String orderNumber, BigDecimal totalAmount,
                                BigDecimal discountAmount, String orderStatus, String paymentStatus, String message) {
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.totalAmount = totalAmount;
            this.discountAmount = discountAmount;
            this.orderStatus = orderStatus;
            this.paymentStatus = paymentStatus;
            this.message = message;
        }

        public Long getOrderId() { return orderId; }
        public String getOrderNumber() { return orderNumber; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public String getOrderStatus() { return orderStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getMessage() { return message; }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}