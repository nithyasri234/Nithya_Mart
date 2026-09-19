package com.nithyamart.util;

public class MockChatProvider implements ChatProvider {

    @Override
    public String reply(String message) {

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("hello")
                || lowerMessage.contains("hi")
                || lowerMessage.contains("hey")) {

            return "Hello! Welcome to NithyaMart. How can I help you?";
        }

        if (lowerMessage.contains("product")
                || lowerMessage.contains("products")) {

            return "You can browse products from the NithyaMart home page.";
        }

        if (lowerMessage.contains("cart")) {

            return "You can add products to your cart and update or remove items from the cart.";
        }

        if (lowerMessage.contains("order")
                || lowerMessage.contains("orders")) {

            return "You can place an order through checkout and view your order history after login.";
        }

        if (lowerMessage.contains("payment")
                || lowerMessage.contains("pay")) {

            return "NithyaMart currently uses a mock payment confirmation for checkout.";
        }

        if (lowerMessage.contains("seller")) {

            return "Sellers can add, update, and delete their products after logging in.";
        }

        if (lowerMessage.contains("help")) {

            return "I can help with products, cart, orders, payment, and seller features.";
        }

        return "Sorry, I am a demo chatbot. Please ask me about products, cart, orders, payment, or sellers.";
    }
}