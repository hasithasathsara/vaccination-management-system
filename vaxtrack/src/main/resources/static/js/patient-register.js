// Live digital ID card preview during registration

document.addEventListener("DOMContentLoaded", function () {

    const nameInput = document.getElementById("fullName");
    const idNumberInput = document.getElementById("idNumber");
    const ageInput = document.getElementById("age");
    const phoneInput = document.getElementById("phoneNumber");

    const cardName = document.getElementById("cardName");
    const cardIdNumber = document.getElementById("cardIdNumber");
    const cardAge = document.getElementById("cardAge");
    const cardPhone = document.getElementById("cardPhone");

    // Copies a text input's value onto the card, live
    function syncField(input, cardElement, placeholder) {
        if (!input || !cardElement) {
            return;
        }
        input.addEventListener("input", function () {
            cardElement.textContent = input.value.trim() || placeholder;
        });
    }

    syncField(nameInput, cardName, "Your Name");
    syncField(idNumberInput, cardIdNumber, "ID Number");
    syncField(ageInput, cardAge, "—");
    syncField(phoneInput, cardPhone, "Phone Number");

    // Toggles password field visibility
    window.togglePassword = function (inputId, iconId) {
        const passwordInput = document.getElementById(inputId);
        const eyeIcon = document.getElementById(iconId);
        const isHidden = passwordInput.type === "password";

        passwordInput.type = isHidden ? "text" : "password";
        eyeIcon.src = isHidden ? eyeIcon.dataset.showIcon : eyeIcon.dataset.hideIcon;
    };

});
