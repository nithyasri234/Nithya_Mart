/**
 * NithyaMart Session & Navigation Manager
 * Handles dynamic role-based headers, user identities, cart count, and logout.
 */
let currentUserSession = null;

async function loadSession() {
    const loginLink = document.getElementById("loginLink");
    const signupLink = document.getElementById("signupLink");
    const logoutLink = document.getElementById("logoutLink");
    const cartCountElements = document.querySelectorAll(".cart-count");

    try {
        const response = await fetch("api/v1/session");

        if (!response.ok) {
            currentUserSession = null;
            showLoggedOutNavbar();
            updateGuestCartCount();
            return;
        }

        const session = await response.json();

        if (session && session.loggedIn) {
            currentUserSession = session;
            showLoggedInNavbar(session);
            loadCartCount();
        } else {
            currentUserSession = null;
            showLoggedOutNavbar();
            updateGuestCartCount();
        }

    } catch (error) {
        console.error("Session check failed:", error);
        currentUserSession = null;
        showLoggedOutNavbar();
        updateGuestCartCount();
    }

    function showLoggedInNavbar(session) {
        if (loginLink) {
            loginLink.style.display = "inline-flex";

            const role = (session.userRole || "").toUpperCase();
            const displayName = escapeHtml(session.userName || "User");

            // Point Account icon to role-specific destination
            if (role === "SELLER") {
                loginLink.href = "addproduct.html";
                loginLink.innerHTML = `
                    <span class="header-account-icon">🏪</span>
                    <span class="header-account-text">
                        <small>Seller Portal</small>
                        <strong>${displayName}</strong>
                    </span>
                `;
                loginLink.title = "Go to Seller Dashboard";
            } else if (role === "ADMIN") {
                loginLink.href = "admin.html";
                loginLink.innerHTML = `
                    <span class="header-account-icon">🛡️</span>
                    <span class="header-account-text">
                        <small>Admin Access</small>
                        <strong>${displayName}</strong>
                    </span>
                `;
                loginLink.title = "Go to Admin Panel";
            } else {
                loginLink.href = "account.html";
                loginLink.innerHTML = `
                    <span class="header-account-icon">👤</span>
                    <span class="header-account-text">
                        <small>Hello, ${displayName}</small>
                        <strong>My Account</strong>
                    </span>
                `;
                loginLink.title = "View Account, Orders & Addresses";
            }
        }

        if (signupLink) {
            signupLink.style.display = "none";
        }

        if (logoutLink) {
            logoutLink.style.display = "inline-flex";
            logoutLink.textContent = "Logout";
            logoutLink.onclick = function(event) {
                event.preventDefault();
                window.location.href = "logout";
            };
        }

        // Show seller shortcut pill if on home page and role is SELLER
        const sellerHeaderBtn = document.getElementById("sellerHeaderLink");
        if (sellerHeaderBtn) {
            if (session.userRole === "SELLER") {
                sellerHeaderBtn.style.display = "inline-block";
            } else {
                sellerHeaderBtn.style.display = "none";
            }
        }

        // Show admin shortcut pill if on home page and role is ADMIN
        const adminHeaderBtn = document.getElementById("adminHeaderLink");
        if (adminHeaderBtn) {
            if (session.userRole === "ADMIN") {
                adminHeaderBtn.style.display = "inline-block";
            } else {
                adminHeaderBtn.style.display = "none";
            }
        }

        const userNameElement = document.getElementById("userName");
        if (userNameElement && session.userName) {
            userNameElement.textContent = "Welcome, " + session.userName;
        }
    }

    function showLoggedOutNavbar() {
        if (loginLink) {
            loginLink.style.display = "inline-flex";
            loginLink.href = "login.html";
            loginLink.innerHTML = `
                <span class="header-account-icon">👤</span>
                <span class="header-account-text">
                    <small>Hello, Sign in</small>
                    <strong>Account</strong>
                </span>
            `;
            loginLink.title = "Sign In to NithyaMart";
        }

        if (signupLink) {
            signupLink.style.display = "inline-flex";
        }

        if (logoutLink) {
            logoutLink.style.display = "none";
        }

        const sellerHeaderBtn = document.getElementById("sellerHeaderLink");
        if (sellerHeaderBtn) sellerHeaderBtn.style.display = "none";

        const adminHeaderBtn = document.getElementById("adminHeaderLink");
        if (adminHeaderBtn) adminHeaderBtn.style.display = "none";
    }

    async function loadCartCount() {
        try {
            const res = await fetch("api/v1/cart");
            if (res.ok) {
                const items = await res.json();
                const totalCount = Array.isArray(items) 
                    ? items.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0)
                    : 0;
                updateCartCountDisplay(totalCount);
            } else {
                updateGuestCartCount();
            }
        } catch (e) {
            updateGuestCartCount();
        }
    }

    function updateGuestCartCount() {
        try {
            const localCart = JSON.parse(localStorage.getItem("nm_guest_cart") || "[]");
            const totalCount = Array.isArray(localCart)
                ? localCart.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0)
                : 0;
            updateCartCountDisplay(totalCount);
        } catch {
            updateCartCountDisplay(0);
        }
    }

    function updateCartCountDisplay(count) {
        cartCountElements.forEach(el => {
            el.textContent = String(count);
        });
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}

// Global helper to refresh cart badge from any script
window.refreshGlobalCartCount = function() {
    loadSession();
};

document.addEventListener("DOMContentLoaded", function() {
    loadSession();
});