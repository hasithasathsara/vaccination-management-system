// ============================================================
// VaxTrack Admin — shared shell behaviour
// Sidebar toggle, profile dropdown, generic modal helpers,
// a live table search filter, and flash message auto-dismiss.
// All plain JavaScript on purpose — no framework needed for this.
// ============================================================

document.addEventListener("DOMContentLoaded", function () {

    // --- Sidebar toggle (mobile hamburger) ---
    const hamburgerBtn = document.getElementById("hamburgerBtn");
    const sidebar = document.getElementById("sidebar");

    if (hamburgerBtn && sidebar) {
        hamburgerBtn.addEventListener("click", function () {
            sidebar.classList.toggle("open");
        });
    }

    // --- Profile dropdown ---
    const profileTrigger = document.getElementById("profileTrigger");
    const profileDropdown = document.getElementById("profileDropdown");

    if (profileTrigger && profileDropdown) {
        profileTrigger.addEventListener("click", function (event) {
            event.stopPropagation();
            profileDropdown.classList.toggle("open");
        });

        // Close the dropdown if the user clicks anywhere else on the page.
        document.addEventListener("click", function () {
            profileDropdown.classList.remove("open");
        });
    }

    // --- Close a modal if the user clicks the dark overlay behind it ---
    // (Modals marked "no-outside-close" — like the one-time generated password screen —
    // must be dismissed with their own button, so an accidental click can't lose the password.)
    document.querySelectorAll(".modal-overlay").forEach(function (overlay) {
        overlay.addEventListener("click", function (event) {
            if (event.target === overlay && !overlay.classList.contains("no-outside-close")) {
                overlay.classList.remove("open");
            }
        });
    });

    // --- Live search filter: works for ANY search input with a data-table-target attribute ---
    // Each module's search box points at its own table by id, e.g.
    // <input class="search-input" data-table-target="hospitalTable">
    document.querySelectorAll(".search-input[data-table-target]").forEach(function (input) {
        const targetTable = document.getElementById(input.dataset.tableTarget);
        if (!targetTable) {
            return;
        }
        input.addEventListener("input", function () {
            const query = input.value.trim().toLowerCase();
            const rows = targetTable.querySelectorAll("tbody tr");
            rows.forEach(function (row) {
                const rowText = row.textContent.toLowerCase();
                row.style.display = rowText.includes(query) ? "" : "none";
            });
        });
    });

    // --- Auto-dismiss the flash message banner after a few seconds ---
    const flashBanner = document.getElementById("flashBanner");
    if (flashBanner) {
        setTimeout(function () {
            flashBanner.style.transition = "opacity 0.3s ease";
            flashBanner.style.opacity = "0";
            setTimeout(function () {
                flashBanner.remove();
            }, 300);
        }, 4000);
    }

});

// --- Generic modal open/close, used by inline onclick="" attributes in the templates ---
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add("open");
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove("open");
    }
}

// --- Copies the generated temporary password to the clipboard (used on the
// one-time "Sub-Admin Account Created" screen) and briefly confirms it worked. ---
function copyGeneratedPassword(button) {
    const passwordText = document.getElementById("generatedPasswordText");
    if (!passwordText) {
        return;
    }
    navigator.clipboard.writeText(passwordText.textContent).then(function () {
        const originalLabel = button.textContent;
        button.textContent = "✓ Copied!";
        setTimeout(function () {
            button.textContent = originalLabel;
        }, 1500);
    });
}
