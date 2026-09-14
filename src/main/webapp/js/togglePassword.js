const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const showPasswordCheckbox = document.getElementById('showPassword');
const showConfirmPasswordCheckbox = document.getElementById('showConfirmPassword');
const errorMsg = document.getElementById('errorMsg');
showPasswordCheckbox.addEventListener('change', function() {
    passwordInput.type = this.checked ? 'text' : 'password';
});
showConfirmPasswordCheckbox.addEventListener('change', function() {
    confirmPasswordInput.type = this.checked ? 'text' : 'password';
});
function validateForm() {
    if (passwordInput.value !== confirmPasswordInput.value) {
      errorMsg.style.display = 'block';
      return false; // Blocks form transmission
    }
    errorMsg.style.display = 'none';
    return true; 
}
function hideError() {
    errorMsg.style.display = 'none';
}