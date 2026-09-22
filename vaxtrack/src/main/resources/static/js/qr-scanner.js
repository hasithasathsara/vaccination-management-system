// Camera-based QR scanning for the Scan QR page, with AJAX lookup so a successful
// scan (or a typed code) opens a patient popup right on this page — no navigation.

let scannerStream = null;
let scannerLoopId = null;

async function startScanner() {
    const scannerArea = document.getElementById("scannerArea");
    const video = document.getElementById("scannerVideo");

    // Fail loudly and visibly if the scanning library itself didn't load,
    // instead of the camera opening but silently never detecting anything.
    if (typeof jsQR !== "function") {
        showLookupError("The QR scanning library failed to load. Please refresh the page and try again.");
        return;
    }

    try {
        // "ideal" (not a hard requirement) so this still works on devices/browsers
        // that don't support facingMode constraints at all, instead of throwing
        // an OverconstrainedError and silently failing.
        scannerStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: { ideal: "environment" } }
        });
    } catch (error) {
        try {
            // Fallback: ask for ANY camera, no facing preference at all
            scannerStream = await navigator.mediaDevices.getUserMedia({ video: true });
        } catch (fallbackError) {
            showLookupError("Couldn't access a camera on this device (" + fallbackError.name + "). Please type the code manually instead.");
            return;
        }
    }

    video.srcObject = scannerStream;
    video.setAttribute("playsinline", true);
    video.play();
    scannerArea.style.display = "block";
    document.getElementById("startScanBtn").style.display = "none";

    scannerLoopId = requestAnimationFrame(scanFrame);
}

function scanFrame() {
    const video = document.getElementById("scannerVideo");
    const canvas = document.getElementById("scannerCanvas");

    if (video.readyState === video.HAVE_ENOUGH_DATA) {
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        const context = canvas.getContext("2d");
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        const imageData = context.getImageData(0, 0, canvas.width, canvas.height);

        let result = null;
        try {
            result = jsQR(imageData.data, imageData.width, imageData.height);
        } catch (err) {
            // Never let a decode error silently kill the scan loop — show it and stop cleanly
            showLookupError("Scanning error: " + err.message + ". Please try again.");
            stopScanner();
            return;
        }

        if (result && result.data) {
            stopScanner();
            lookupCode(result.data);
            return;
        }
    }

    scannerLoopId = requestAnimationFrame(scanFrame);
}

function stopScanner() {
    if (scannerLoopId) {
        cancelAnimationFrame(scannerLoopId);
        scannerLoopId = null;
    }
    if (scannerStream) {
        scannerStream.getTracks().forEach(function (track) { track.stop(); });
        scannerStream = null;
    }
    document.getElementById("scannerArea").style.display = "none";
    document.getElementById("startScanBtn").style.display = "block";
}

function showLookupError(message) {
    const errorBanner = document.getElementById("lookupError");
    errorBanner.textContent = message;
    errorBanner.style.display = "block";
}

// AJAX lookup — used by both a camera scan result and manual code entry
async function lookupCode(code) {
    const errorBanner = document.getElementById("lookupError");
    errorBanner.style.display = "none";

    if (!code || !code.trim()) {
        showLookupError("Please enter a booking code.");
        return;
    }

    let data;
    try {
        const response = await fetch("/staff/scan-qr/lookup-ajax?code=" + encodeURIComponent(code.trim()));
        data = await response.json();
    } catch (err) {
        showLookupError("Couldn't reach the server. Check your connection and try again.");
        return;
    }

    if (data.error) {
        showLookupError(data.error);
        return;
    }

    // Fill in the popup with what came back
    document.getElementById("pName").textContent = data.patientName;
    document.getElementById("pIdTypeLabel").textContent = data.idType;
    document.getElementById("pIdNumber").textContent = data.idNumber;
    document.getElementById("pAge").textContent = data.age;
    document.getElementById("pPhone").textContent = data.phone;
    document.getElementById("pDisabilities").textContent = data.disabilities;
    document.getElementById("pBookingSummary").textContent =
        data.vaccineName + " — Dose " + data.doseNumber + " of " + data.dosesRequired
        + " • " + data.hospitalName + " • " + data.eventDate + " • " + data.timeSlot;

    const historyBody = document.getElementById("pHistoryBody");
    historyBody.innerHTML = "";
    if (data.history && data.history.length > 0) {
        document.getElementById("pHistoryTable").style.display = "table";
        document.getElementById("pHistoryEmpty").style.display = "none";
        data.history.forEach(function (h) {
            const row = document.createElement("tr");
            row.innerHTML = "<td>" + h.vaccine + "</td><td>" + h.dose + "</td>"
                + "<td><span class=\"badge " + (h.status === "VACCINATED" ? "active" : "inactive") + "\">" + h.status + "</span></td>"
                + "<td>" + h.date + "</td>";
            historyBody.appendChild(row);
        });
    } else {
        document.getElementById("pHistoryTable").style.display = "none";
        document.getElementById("pHistoryEmpty").style.display = "block";
    }

    // Point the action buttons at this specific appointment
    document.getElementById("vaccinateForm").action = "/staff/verify/" + data.appointmentId + "/vaccinate";
    document.getElementById("failForm").action = "/staff/verify/" + data.appointmentId + "/fail";

    openModal("patientModal");
}
