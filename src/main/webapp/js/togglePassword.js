const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const showPasswordCheckbox = document.getElementById('showPassword');
const matchIndicator = document.getElementById('matchIndicator');

// Consolidating to a single requirement box reference selector
const reqPopup = document.querySelector('.requirements-box');


// Target checklist text element reference handles
const reqLength = document.getElementById('req-length');
const reqUpper = document.getElementById('req-case-upper');
const reqLower = document.getElementById('req-case-lower');
const reqNumber = document.getElementById('req-number');
const reqSymbol = document.getElementById('req-symbol');

// 1. Show the dynamic requirements popup block only when entering the field
passwordInput.addEventListener('focus', function() {
    reqPopup.style.display = 'flex';
});

// 2. Hide requirements box when clicking outside. 
// If field is left empty, reveal the black native alert tooltip badge
passwordInput.addEventListener('blur', function() {
    reqPopup.style.display = 'none';
});

// Masking switcher tool option toggler
showPasswordCheckbox.addEventListener('change', function() {
    const type = this.checked ? 'text' : 'password';
    passwordInput.type = type;
    confirmPasswordInput.type = type;
});

// Real-time input validation regex testing strings updates
function validatePassword() {
    const val = passwordInput.value;
    
    // 1. Length restriction check (6-12 characters based on layout screen)
    if (val.length >= 6 && val.length <= 12) {
        reqLength.classList.add('valid');
    } else {
        reqLength.classList.remove('valid');
    }

    // 2. Uppercase letter check
    if (/[A-Z]/.test(val)) {
        reqUpper.classList.add('valid');
    } else {
        reqUpper.classList.remove('valid');
    }

    // 3. Lowercase letter check
    if (/[a-z]/.test(val)) {
        reqLower.classList.add('valid');
    } else {
        reqLower.classList.remove('valid');
    }

    // 4. Number array character check
    if (/\d/.test(val)) {
        reqNumber.classList.add('valid');
    } else {
        reqNumber.classList.remove('valid');
    }

    // 5. Special symbol character check
    if (/[ `!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~]/.test(val)) {
        reqSymbol.classList.add('valid');
    } else {
        reqSymbol.classList.remove('valid');
    }

    checkMatch();
}

// Verification match tracking engine logic block
function checkMatch() {
    const p1 = passwordInput.value;
    const p2 = confirmPasswordInput.value; 
    
    if (p1 === p2 && p1.length > 0) {
        matchIndicator.classList.add('visible');
    } else {
        matchIndicator.classList.remove('visible');
    }
}

// Fire live validation calculations on every keystroke input loop
passwordInput.addEventListener('input', validatePassword);
confirmPasswordInput.addEventListener('input', checkMatch);
