async function loadSession() {

    const loginLink = document.getElementById("loginLink");
    const signupLink = document.getElementById("signupLink");
    const logoutLink = document.getElementById("logoutLink");

    try {

        const response = await fetch("api/v1/session");

        if (!response.ok) {
            showLoggedOutNavbar();
            return;
        }

        const session = await response.json();

        if (session && session.loggedIn) {
            showLoggedInNavbar(session);
        } else {
            showLoggedOutNavbar();
        }

    } catch (error) {

        console.error("Session check failed:", error);

        showLoggedOutNavbar();
    }

    function showLoggedInNavbar(session) {

        if (loginLink) {
            loginLink.style.display = "none";
        }

        if (signupLink) {
            signupLink.style.display = "none";
        }

        if (logoutLink) {
            logoutLink.style.display = "inline-block";

            logoutLink.onclick = function(event) {
                event.preventDefault();
                window.location.href = "logout";
            };
        }

        // Show the logged-in user's name if an element exists
        const userNameElement = document.getElementById("userName");

        if (userNameElement && session.userName) {
            userNameElement.textContent =
                "Welcome, " + session.userName;
        }
    }

    function showLoggedOutNavbar() {

        if (loginLink) {
            loginLink.style.display = "inline-block";
        }

        if (signupLink) {
            signupLink.style.display = "inline-block";
        }

        if (logoutLink) {
            logoutLink.style.display = "none";
        }
    }
}

document.addEventListener("DOMContentLoaded", function() {
    loadSession();
});