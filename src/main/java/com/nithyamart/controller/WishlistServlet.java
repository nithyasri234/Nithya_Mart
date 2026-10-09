package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.WishlistDAO;
import com.nithyamart.model.WishlistItem;
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

@WebServlet(urlPatterns = {"/api/v1/wishlist", "/api/v1/wishlist/*"})
public class WishlistServlet extends HttpServlet {

    private WishlistDAO wishlistDAO;
    private Gson gson;

    public WishlistServlet() {
    }

    public WishlistServlet(WishlistDAO wishlistDAO, Gson gson) {
        this.wishlistDAO = wishlistDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        this.wishlistDAO = new WishlistDAO(dataSource);
        this.gson = JsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long buyerId = getBuyerId(request);
        if (buyerId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Buyer login is required.");
            return;
        }

        try {
            String prodParam = request.getParameter("productId");
            if (prodParam != null && !prodParam.isBlank()) {
                Long productId = Long.parseLong(prodParam.trim());
                boolean inWishlist = wishlistDAO.isInWishlist(buyerId, productId);
                writeJson(response, new InWishlistResponse(inWishlist));
                return;
            }

            List<WishlistItem> items = wishlistDAO.findByBuyerId(buyerId);
            writeJson(response, items);
        } catch (Exception e) {
            getServletContext().log("Unable to load wishlist.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load wishlist.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long buyerId = getBuyerId(request);
        if (buyerId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Buyer login is required.");
            return;
        }

        try {
            Long productId = parseLong(request.getParameter("productId"));
            if (productId == null) {
                try {
                    WishlistRequest req = gson.fromJson(request.getReader(), WishlistRequest.class);
                    if (req != null) {
                        productId = req.productId;
                    }
                } catch (Exception ignored) {
                }
            }

            if (productId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid product ID is required.");
                return;
            }

            wishlistDAO.addItem(buyerId, productId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, new MessageResponse("Product added to wishlist."));
        } catch (Exception e) {
            getServletContext().log("Unable to add to wishlist.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to add to wishlist.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long buyerId = getBuyerId(request);
        if (buyerId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Buyer login is required.");
            return;
        }

        try {
            Long productId = parseLong(request.getParameter("productId"));
            if (productId == null) {
                productId = parseId(request.getPathInfo());
            }

            if (productId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Valid product ID is required.");
                return;
            }

            wishlistDAO.removeItem(buyerId, productId);
            writeJson(response, new MessageResponse("Product removed from wishlist."));
        } catch (Exception e) {
            getServletContext().log("Unable to remove from wishlist.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to remove from wishlist.");
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

    private static class WishlistRequest {
        private Long productId;
    }

    private static class InWishlistResponse {
        private final boolean inWishlist;
        private InWishlistResponse(boolean inWishlist) {
            this.inWishlist = inWishlist;
        }
    }

    private static class MessageResponse {
        private final String message;
        private MessageResponse(String message) {
            this.message = message;
        }
    }
}
