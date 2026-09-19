package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.util.ChatProvider;
import com.nithyamart.util.MockChatProvider;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {

    private ChatProvider chatProvider;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        chatProvider = new MockChatProvider();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        ChatRequest chatRequest =
                gson.fromJson(
                        request.getReader(),
                        ChatRequest.class
                );

        if (chatRequest == null
                || chatRequest.message == null
                || chatRequest.message.isBlank()) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Message is required."
            );

            return;
        }

        try {

            String reply =
                    chatProvider.reply(
                            chatRequest.message.trim()
                    );

            writeJson(
                    response,
                    new ChatResponse(reply)
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Chat request failed.",
                    e
            );

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to process chat request."
            );
        }
    }

    private void writeJson(
            HttpServletResponse response,
            Object data)
            throws IOException {

        response.getWriter()
                .write(gson.toJson(data));
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        writeJson(
                response,
                new ErrorResponse(message)
        );
    }

    private static class ChatRequest {

        private String message;
    }

    private static class ChatResponse {

        private final String reply;

        private ChatResponse(String reply) {
            this.reply = reply;
        }
    }

    private static class ErrorResponse {

        private final String message;

        private ErrorResponse(String message) {
            this.message = message;
        }
    }
}