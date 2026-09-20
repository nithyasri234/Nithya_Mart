package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.ProductDAO;
import com.nithyamart.model.Product;
import com.nithyamart.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/v1/products/*")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
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

        ProductDAO productDAO =
                new ProductDAO(dataSource);

        productService =
                new ProductService(productDAO);

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

            String path =
                    request.getPathInfo();

            if (path == null
                    || path.equals("/")
                    || path.isBlank()) {

                String keyword =
                        request.getParameter("keyword");

                String category =
                        request.getParameter("category");

                List<Product> products =
                        productService.search(
                                keyword,
                                category
                        );

                writeJson(
                        response,
                        products
                );

                return;
            }

            Long productId =
                    parseId(path);

            if (productId == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID."
                );

                return;
            }

            Optional<Product> product =
                    productService.findById(productId);

            if (product.isEmpty()) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found."
                );

                return;
            }

            writeJson(
                    response,
                    product.get()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to load products.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load products."
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

            HttpSession session =
                    request.getSession(false);

            if (!isSeller(session)) {

                sendError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Seller login is required."
                );

                return;
            }

            Product product =
                    gson.fromJson(
                            request.getReader(),
                            Product.class
                    );

            Long sellerId =
                    (Long) session.getAttribute("userId");

            product.setSellerId(sellerId);

            Product created =
                    productService.createProduct(product);

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            writeJson(
                    response,
                    created
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to create product.",
                    e
            );

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.setContentType(
                    "text/plain;charset=UTF-8"
            );

            e.printStackTrace(
                    response.getWriter()
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

            HttpSession session =
                    request.getSession(false);

            if (!isSeller(session)) {

                sendError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Seller login is required."
                );

                return;
            }

            String path =
                    request.getPathInfo();

            Long productId =
                    parseId(path);

            if (productId == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID."
                );

                return;
            }

            Product product =
                    gson.fromJson(
                            request.getReader(),
                            Product.class
                    );

            Long sellerId =
                    (Long) session.getAttribute("userId");

            product.setId(productId);
            product.setSellerId(sellerId);

            boolean updated =
                    productService.updateProduct(
                            product,
                            sellerId
                    );

            if (!updated) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found."
                );

                return;
            }

            writeJson(
                    response,
                    product
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to update product.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update product."
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

            HttpSession session =
                    request.getSession(false);

            if (!isSeller(session)) {

                sendError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Seller login is required."
                );

                return;
            }

            String path =
                    request.getPathInfo();

            Long productId =
                    parseId(path);

            if (productId == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID."
                );

                return;
            }

            Long sellerId =
                    (Long) session.getAttribute("userId");

            boolean deleted =
                    productService.deleteProduct(
                            productId,
                            sellerId
                    );

            if (!deleted) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found."
                );

                return;
            }

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to delete product.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to delete product."
            );
        }
    }

    private boolean isSeller(HttpSession session) {

        if (session == null) {
            return false;
        }

        Object role =
                session.getAttribute("userRole");

        return "SELLER".equals(role);
    }

    private Long parseId(String path) {

        if (path == null || path.isBlank()) {
            return null;
        }

        String idText =
                path.substring(1);

        try {
            return Long.parseLong(idText);
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

        response.getWriter()
                .write(
                        gson.toJson(
                                new ErrorResponse(message)
                        )
                );
    }

    private static class ErrorResponse {

        @SuppressWarnings("unused")
        private final String message;

        private ErrorResponse(String message) {
            this.message = message;
        }
    }
}