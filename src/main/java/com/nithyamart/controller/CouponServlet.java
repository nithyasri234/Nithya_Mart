package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.CouponDAO;
import com.nithyamart.model.Coupon;
import com.nithyamart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/v1/coupons", "/api/v1/coupons/*"})
public class CouponServlet extends HttpServlet {

    private CouponDAO couponDAO;
    private Gson gson;

    public CouponServlet() {
    }

    public CouponServlet(CouponDAO couponDAO, Gson gson) {
        this.couponDAO = couponDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        couponDAO = new CouponDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            List<Coupon> activeCoupons = couponDAO.findAllActive();
            writeJson(response, activeCoupons);
        } catch (Exception e) {
            getServletContext().log("Unable to load coupons", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load coupons.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String path = request.getPathInfo();
        try {
            String code = request.getParameter("code");
            String subtotalStr = request.getParameter("subtotal");

            if (code == null || code.isBlank()) {
                try {
                    Map<String, Object> body = gson.fromJson(request.getReader(), Map.class);
                    if (body != null) {
                        if (body.get("code") != null) {
                            code = body.get("code").toString();
                        }
                        if (body.get("subtotal") != null) {
                            subtotalStr = body.get("subtotal").toString();
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            if (code == null || code.isBlank()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Coupon code is required.");
                return;
            }

            Coupon coupon = couponDAO.findByCode(code.trim());
            if (coupon == null || !coupon.isActive()) {
                writeJson(response, new CouponValidationResult(false, "Invalid or expired coupon code.", BigDecimal.ZERO, code));
                return;
            }

            BigDecimal subtotal = BigDecimal.ZERO;
            if (subtotalStr != null && !subtotalStr.isBlank()) {
                try {
                    subtotal = new BigDecimal(subtotalStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }

            if (coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
                writeJson(response, new CouponValidationResult(
                        false,
                        "Order subtotal must be at least ₹" + coupon.getMinOrderValue() + " to use this coupon.",
                        BigDecimal.ZERO,
                        coupon.getCode()
                ));
                return;
            }

            BigDecimal discount = couponDAO.calculateDiscount(coupon, subtotal);
            writeJson(response, new CouponValidationResult(
                    true,
                    "Coupon applied successfully! You save ₹" + discount + ".",
                    discount,
                    coupon.getCode()
            ));

        } catch (Exception e) {
            getServletContext().log("Unable to validate coupon", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to validate coupon.");
        }
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new ErrorResponse(message));
    }

    private static class CouponValidationResult {
        private final boolean valid;
        private final String message;
        private final BigDecimal discountAmount;
        private final String code;

        public CouponValidationResult(boolean valid, String message, BigDecimal discountAmount, String code) {
            this.valid = valid;
            this.message = message;
            this.discountAmount = discountAmount;
            this.code = code;
        }
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
