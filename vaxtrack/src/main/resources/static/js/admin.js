

document.addEventListener("DOMContentLoaded", function () {

    //  Sidebar toggle
    const hamburgerBtn = document.getElementById("hamburgerBtn");
    const sidebar = document.getElementById("sidebar");

    if (hamburgerBtn && sidebar) {
        hamburgerBtn.addEventListener("click", function () {
            sidebar.classList.toggle("open");
        });
    }

    //Profile dropdown
    const profileTrigger = document.getElementById("profileTrigger");
    const profileDropdown = document.getElementById("profileDropdown");

    if (profileTrigger && profileDropdown) {
        profileTrigger.addEventListener("click", function (event) {
            event.stopPropagation();
            profileDropdown.classList.toggle("open");
        });


        document.addEventListener("click", function () {
            profileDropdown.classList.remove("open");
        });
    }


    document.querySelectorAll(".modal-overlay").forEach(function (overlay) {
        overlay.addEventListener("click", function (event) {
            if (event.target === overlay) {
                overlay.classList.remove("open");
            }
        });
    });


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
