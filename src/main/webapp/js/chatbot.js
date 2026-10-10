
document.addEventListener("DOMContentLoaded", function () {
    const chatButton = document.getElementById("chatbot-toggle");

    if (chatButton) {
        chatButton.addEventListener("click", function () {
            alert("Chatbot button is working!");
        });
    } else {
        console.error("Chatbot button not found in HTML");
    }
});
