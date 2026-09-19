package com.nithyamart.controller;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/api/v1/session")
public class SessionServlet extends HttpServlet {

    private Gson gson;

    @Override
    public void init() throws ServletException {
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            writeJson(
                    response,
                    new SessionResponse(
                            false,
                            null,
                            null,
                            null,
                            null
                    )
            );

            return;
        }

        Long userId =
                (Long) session.getAttribute("userId");

        String userName =
                (String) session.getAttribute("userName");

        String userEmail =
                (String) session.getAttribute("userEmail");

        String userRole =
                (String) session.getAttribute("userRole");

        writeJson(
                response,
                new SessionResponse(
                        true,
                        userId,
                        userName,
                        userEmail,
                        userRole
                )
        );
    }

    private void writeJson(
            HttpServletResponse response,
            Object data)
            throws IOException {

        response.getWriter()
                .write(gson.toJson(data));
    }

    private static class SessionResponse {

        private final boolean loggedIn;
        private final Long userId;
        private final String userName;
        private final String userEmail;
        private final String userRole;

        private SessionResponse(
                boolean loggedIn,
                Long userId,
                String userName,
                String userEmail,
                String userRole) {

            this.loggedIn = loggedIn;
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.userRole = userRole;
        }
    }
}