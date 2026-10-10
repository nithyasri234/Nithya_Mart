
document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById("chatbot-toggle");
    const close = document.getElementById("chatbot-close");
    const windowEl = document.getElementById("chatbot-window");
    const form = document.getElementById("chatbot-form");
    const input = document.getElementById("chatbot-input");
    const messages = document.getElementById("chatbot-messages");

    if (!toggle || !close || !windowEl || !form || !input || !messages) {
        console.error("Chatbot HTML elements are missing.");
        return;
    }

    function openChat() {
        windowEl.classList.add("open");
        windowEl.setAttribute("aria-hidden", "false");
        toggle.setAttribute("aria-expanded", "true");
        input.focus();
    }

    function closeChat() {
        windowEl.classList.remove("open");
        windowEl.setAttribute("aria-hidden", "true");
        toggle.setAttribute("aria-expanded", "false");
    }

    function addMessage(text, sender) {
        const message = document.createElement("div");
        message.className = `chatbot-message ${sender}`;
        message.textContent = text;
        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    }

    toggle.addEventListener("click", () => {
        if (windowEl.classList.contains("open")) {
            closeChat();
        } else {
            openChat();
        }
    });

    close.addEventListener("click", closeChat);

    // Temporary response to test the interface.
    // We will replace this with the real AI backend connection.
    form.addEventListener("submit", (event) => {
        event.preventDefault();

        const question = input.value.trim();
        if (!question) return;

        addMessage(question, "user");
        input.value = "";

        addMessage(
            "Thanks for your question! The AI connection will be added in the next step.",
            "bot"
        );
    });
});
