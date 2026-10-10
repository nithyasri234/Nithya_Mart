
console.log("CHATBOT JS LOADED");

document.addEventListener("DOMContentLoaded", function () {
    const toggle = document.getElementById("chatbot-toggle");
    const chatWindow = document.getElementById("chatbot-window");
    const closeButton = document.getElementById("chatbot-close");
    const form = document.getElementById("chatbot-form");
    const input = document.getElementById("chatbot-input");
    const messages = document.getElementById("chatbot-messages");

    if (!toggle || !chatWindow || !closeButton ||
        !form || !input || !messages) {
        console.error("Chatbot HTML elements are missing.");
        return;
    }

    toggle.addEventListener("click", function () {
        chatWindow.classList.toggle("chatbot-hidden");
    });

    closeButton.addEventListener("click", function () {
        chatWindow.classList.add("chatbot-hidden");
    });

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        const question = input.value.trim();
        if (!question) return;

        addMessage(question, "user-message");

        addMessage(
            "I'm currently in demo mode. AI integration is not connected yet.",
            "bot-message"
        );

        input.value = "";
    });

    function addMessage(text, className) {
        const message = document.createElement("div");
        message.className = className;
        message.textContent = text;
        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    }
});
