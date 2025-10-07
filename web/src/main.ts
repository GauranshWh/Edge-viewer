// Inside web/src/main.ts

document.addEventListener("DOMContentLoaded", () => {
    const imgElement = document.getElementById("processed-image") as HTMLImageElement;
    if (imgElement) {
        // This line MUST match your image's filename exactly
        imgElement.src = "sample_frame.jpeg"; 
    }
});