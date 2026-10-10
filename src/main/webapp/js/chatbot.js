
console.log("CHATBOT JS LOADED");
document.addEventListener("DOMContentLoaded", function () {
    const toggle = document.getElementById("chatbot-toggle");
    const chatWindow = document.getElementById("chatbot-window");
    const closeButton = document.getElementById("chatbot-close");
    const form = document.getElementById("chatbot-form");
    const input = document.getElementById("chatbot-input");
    const messages = document.getElementById("chatbot-messages");

    if (!toggle || !chatWindow || !closeButton || !form) {
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

        const userMessage = document.createElement("div");
        userMessage.className = "user-message";
        userMessage.textContent = question;
        messages.appendChild(userMessage);

        const botMessage = document.createElement("div");
        botMessage.className = "bot-message";
        botMessage.textContent =
            "Thanks for your question! AI integration is the next step.";
        messages.appendChild(botMessage);

        input.value = "";
        messages.scrollTop = messages.scrollHeight;
    });
});
