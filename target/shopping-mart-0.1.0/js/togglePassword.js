const passwordInput = document.getElementById("password");
const confirmPasswordInput = document.getElementById("confirmPassword");
const showPasswordCheckbox = document.getElementById("showPassword");

const matchIndicator = document.getElementById("matchIndicator");
const reqPopup = document.querySelector(".requirements-box");

const reqLength = document.getElementById("req-length");
const reqUpper = document.getElementById("req-case-upper");
const reqLower = document.getElementById("req-case-lower");
const reqNumber = document.getElementById("req-number");
const reqSymbol = document.getElementById("req-symbol");


// Show password requirements when password field is focused
passwordInput.addEventListener("focus", function () {
    reqPopup.style.display = "flex";
});


// Hide password requirements when password field loses focus
passwordInput.addEventListener("blur", function () {
    reqPopup.style.display = "none";
});


// Show / hide password
showPasswordCheckbox.addEventListener("change", function () {

    const type = this.checked ? "text" : "password";

    passwordInput.type = type;
    confirmPasswordInput.type = type;
});


// Validate password requirements
function validatePassword() {

    const password = passwordInput.value;


    // 1. Length: 6 to 12 characters
    if (password.length >= 6 && password.length <= 12) {
        reqLength.classList.add("valid");
    } else {
        reqLength.classList.remove("valid");
    }


    // 2. Uppercase letter
    if (/[A-Z]/.test(password)) {
        reqUpper.classList.add("valid");
    } else {
        reqUpper.classList.remove("valid");
    }


    // 3. Lowercase letter
    if (/[a-z]/.test(password)) {
        reqLower.classList.add("valid");
    } else {
        reqLower.classList.remove("valid");
    }


    // 4. Number
    if (/[0-9]/.test(password)) {
        reqNumber.classList.add("valid");
    } else {
        reqNumber.classList.remove("valid");
    }


    // 5. Special character
    if (/[^A-Za-z0-9]/.test(password)) {
        reqSymbol.classList.add("valid");
    } else {
        reqSymbol.classList.remove("valid");
    }


    checkMatch();
}


// Check whether passwords match
function checkMatch() {

    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;


    if (
        password.length > 0 &&
        password === confirmPassword
    ) {
        matchIndicator.classList.add("visible");
    } else {
        matchIndicator.classList.remove("visible");
    }
}


// Validate password while typing
passwordInput.addEventListener("input", validatePassword);


// Check password match while typing
confirmPasswordInput.addEventListener("input", checkMatch);