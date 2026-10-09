package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.AddressDAO;
import com.nithyamart.model.Address;
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
import java.util.Optional;

@WebServlet(urlPatterns = {"/api/v1/addresses", "/api/v1/addresses/*"})
public class AddressServlet extends HttpServlet {

    private AddressDAO addressDAO;
    private Gson gson;

    public AddressServlet() {
    }

    public AddressServlet(AddressDAO addressDAO, Gson gson) {
        this.addressDAO = addressDAO;
        this.gson = gson != null ? gson : JsonUtil.getGson();
    }

    @Override
    public void init() throws ServletException {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        if (dataSource == null) {
            throw new ServletException("DataSource is not available.");
        }
        this.addressDAO = new AddressDAO(dataSource);
        this.gson = JsonUtil.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long userId = getUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return;
        }

        try {
            Long addressId = parseId(request.getPathInfo());
            if (addressId != null) {
                Optional<Address> address = addressDAO.findById(addressId, userId);
                if (address.isEmpty()) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found.");
                    return;
                }
                writeJson(response, address.get());
                return;
            }

            List<Address> addresses = addressDAO.findByUserId(userId);
            writeJson(response, addresses);
        } catch (Exception e) {
            getServletContext().log("Unable to load addresses.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load addresses.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long userId = getUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return;
        }

        try {
            Address address = gson.fromJson(request.getReader(), Address.class);
            if (address == null || address.getFullName() == null || address.getFullName().isBlank()
                    || address.getAddressLine() == null || address.getAddressLine().isBlank()
                    || address.getCity() == null || address.getCity().isBlank()
                    || address.getState() == null || address.getState().isBlank()
                    || address.getPostalCode() == null || address.getPostalCode().isBlank()
                    || address.getPhone() == null || address.getPhone().isBlank()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "All address fields are required.");
                return;
            }

            address.setUserId(userId);
            Address created = addressDAO.create(address);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, created);
        } catch (Exception e) {
            getServletContext().log("Unable to create address.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to save address.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long userId = getUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return;
        }

        try {
            String path = request.getPathInfo();
            if (path != null && path.endsWith("/default")) {
                String idStr = path.substring(1, path.indexOf("/default"));
                Long id = Long.parseLong(idStr);
                boolean updated = addressDAO.setDefault(id, userId);
                if (!updated) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found.");
                    return;
                }
                writeJson(response, new MessageResponse("Default address updated."));
                return;
            }

            Long addressId = parseId(path);
            if (addressId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Address ID is required.");
                return;
            }

            Address address = gson.fromJson(request.getReader(), Address.class);
            if (address == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid address data.");
                return;
            }

            address.setId(addressId);
            address.setUserId(userId);
            boolean updated = addressDAO.update(address);
            if (!updated) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found.");
                return;
            }

            writeJson(response, address);
        } catch (Exception e) {
            getServletContext().log("Unable to update address.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to update address.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Long userId = getUserId(request);
        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return;
        }

        try {
            Long addressId = parseId(request.getPathInfo());
            if (addressId == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Address ID is required.");
                return;
            }

            boolean deleted = addressDAO.delete(addressId, userId);
            if (!deleted) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Address not found.");
                return;
            }

            writeJson(response, new MessageResponse("Address deleted successfully."));
        } catch (Exception e) {
            getServletContext().log("Unable to delete address.", e);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to delete address.");
        }
    }

    private Long getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
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

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new MessageResponse(message));
    }

    private static class MessageResponse {
        private final String message;
        private MessageResponse(String message) {
            this.message = message;
        }
    }
}
