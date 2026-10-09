package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.ReviewDAO;
import com.nithyamart.model.Review;
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

@WebServlet(urlPatterns = {"/api/v1/reviews", "/api/v1/reviews/*"})
public class ReviewServlet extends HttpServlet {

    private ReviewDAO reviewDAO;
    private Gson gson;

    public ReviewServlet() {
    }

    public ReviewServlet(ReviewDAO reviewDAO, Gson gson) {
        this.reviewDAO = reviewDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        this.reviewDAO = new ReviewDAO(dataSource);
        this.gson = JsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            Long productId = parseLong(request.getParameter("productId"));
            if (productId == null) {
                productId = parseId(request.getPathInfo());
            }

            if (productId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid product ID is required.");
                return;
            }

            List<Review> reviews = reviewDAO.findByProductId(productId);
            double averageRating = reviewDAO.getAverageRating(productId);
            int totalReviews = reviewDAO.getReviewCount(productId);

            writeJson(response, new ProductReviewsResponse(productId, averageRating, totalReviews, reviews));
        } catch (Exception e) {
            getServletContext().log("Unable to load reviews.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load reviews.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Long buyerId = getBuyerId(request);
        if (buyerId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Buyer login is required to review products.");
            return;
        }

        try {
            ReviewInput input = gson.fromJson(request.getReader(), ReviewInput.class);
            if (input == null || input.productId == null || input.rating == null
                    || input.rating < 1 || input.rating > 5) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Product ID and a rating between 1 and 5 are required.");
                return;
            }

            Review review = new Review(
                    input.orderId != null ? input.orderId : 0L,
                    input.productId,
                    buyerId,
                    input.rating,
                    input.comment != null ? input.comment.trim() : ""
            );

            Review created = reviewDAO.create(review);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, created);
        } catch (Exception e) {
            getServletContext().log("Unable to post review.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save review.");
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

    private Long parseLong(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new MessageResponse(message));
    }

    private static class ReviewInput {
        private Long orderId;
        private Long productId;
        private Integer rating;
        private String comment;
    }

    public static class ProductReviewsResponse {
        private final Long productId;
        private final double averageRating;
        private final int totalReviews;
        private final List<Review> reviews;

        public ProductReviewsResponse(Long productId, double averageRating, int totalReviews, List<Review> reviews) {
            this.productId = productId;
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.reviews = reviews;
        }

        public Long getProductId() { return productId; }
        public double getAverageRating() { return averageRating; }
        public int getTotalReviews() { return totalReviews; }
        public List<Review> getReviews() { return reviews; }
    }

    private static class MessageResponse {
        private final String message;
        private MessageResponse(String message) {
            this.message = message;
        }
    }
}
