// Camera-based QR scanning for the Scan QR page.
// Falls back gracefully on devices/browsers with no camera (e.g. desktop PCs) —
// the manual code entry field always works regardless.

let scannerStream = null;
let scannerLoopId = null;

async function startScanner() {
    const scannerArea = document.getElementById("scannerArea");
    const video = document.getElementById("scannerVideo");

    try {
        scannerStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: "environment" }
        });
    } catch (error) {
        alert("Couldn't access a camera on this device. Please type the code manually instead.");
        return;
    }

    video.srcObject = scannerStream;
    video.setAttribute("playsinline", true);
    video.play();
    scannerArea.style.display = "block";

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
        const result = jsQR(imageData.data, imageData.width, imageData.height);

        if (result && result.data) {
            document.getElementById("codeInput").value = result.data;
            stopScanner();
            document.getElementById("codeInput").closest("form").submit();
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
}
