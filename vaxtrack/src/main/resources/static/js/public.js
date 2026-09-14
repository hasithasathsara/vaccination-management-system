
document.addEventListener("DOMContentLoaded", function () {

    const themeToggle = document.getElementById("themeToggle");
    const themeIcon = document.getElementById("themeIcon");
    const savedTheme = localStorage.getItem("vaxtrack-theme") || "light";

    applyTheme(savedTheme);

    if (themeToggle) {
        themeToggle.addEventListener("click", function () {
            const current = document.documentElement.getAttribute("data-theme") || "light";
            const next = current === "light" ? "dark" : "light";
            applyTheme(next);
            localStorage.setItem("vaxtrack-theme", next);
        });
    }

    function applyTheme(theme) {
        if (theme === "dark") {
            document.documentElement.setAttribute("data-theme", "dark");
            if (themeIcon) themeIcon.textContent = "☀️";
        } else {
            document.documentElement.removeAttribute("data-theme");
            if (themeIcon) themeIcon.textContent = "🌙";
        }
    }

    const hamburger = document.getElementById("navbarHamburger");
    const navLinks = document.getElementById("navbarLinks");
    if (hamburger && navLinks) {
        hamburger.addEventListener("click", function () {
            navLinks.classList.toggle("open");
        });
    }

    const animatedElements = document.querySelectorAll(".fade-in-up");
    if ("IntersectionObserver" in window && animatedElements.length) {
        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add("in-view");
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        animatedElements.forEach(function (el) {
            observer.observe(el);
        });
    } else {
        animatedElements.forEach(function (el) {
            el.classList.add("in-view");
        });
    }

});

function toggleFaq(button) {
    const answer = button.nextElementSibling;
    const isOpen = button.classList.contains("open");

    button.classList.toggle("open", !isOpen);
    answer.classList.toggle("open", !isOpen);

    const icon = button.querySelector("span");
    if (icon) {
        icon.textContent = isOpen ? "+" : "×";
    }
}

function submitContactForm(event) {
    event.preventDefault();
    const form = document.getElementById("contactForm");
    const successMsg = document.getElementById("contactSuccessMsg");

    if (successMsg) {
        successMsg.style.display = "block";
    }
    form.reset();

    return false;
}
