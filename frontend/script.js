document.addEventListener("DOMContentLoaded", () => {
    const billForm = document.getElementById("billForm");
    const submitBtn = document.getElementById("submitBtn");
    const errorBox = document.getElementById("errorBox");
    const receiptSection = document.getElementById("receiptSection");
    const printBtn = document.getElementById("printBtn");

    // Handle form submission
    billForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        // Clear previous error messages
        hideError();

        // 1. Collect form inputs
        const formData = new FormData(billForm);
        const params = new URLSearchParams();

        for (const [key, value] of formData.entries()) {
            params.append(key, value.trim());
        }

        // Keep track of billing days for receipt display
        const billingDays = formData.get("days");

        // 2. Set button loading state
        submitBtn.disabled = true;
        submitBtn.textContent = "Calculating Bill...";

        try {
            // 3. Send HTTP POST request to Java backend (application/x-www-form-urlencoded)
            const response = await fetch("/calculate", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
                },
                body: params.toString()
            });

            const data = await response.json();

            // 4. Handle backend validation errors
            if (!response.ok || data.error) {
                showError(data.error || "Failed to calculate electricity bill.");
                receiptSection.classList.add("hidden");
                return;
            }

            // 5. Populate and display the bill receipt received from Java
            displayReceipt(data, billingDays);

        } catch (err) {
            showError("Network error: Unable to reach Java backend server at http://localhost:8080/.");
            receiptSection.classList.add("hidden");
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Generate Electricity Bill";
        }
    });

    // Handle receipt printing
    printBtn.addEventListener("click", () => {
        window.print();
    });

    // Helper: Display data returned from Java
    function displayReceipt(bill, days) {
        document.getElementById("resName").textContent = bill.consumerName;
        document.getElementById("resId").textContent = bill.connectionId;
        document.getElementById("resType").textContent = bill.connectionType;
        document.getElementById("resDays").textContent = `${days} Days`;

        // All calculated values originate from the Java backend
        document.getElementById("resUnits").textContent = Number(bill.units).toFixed(2);
        document.getElementById("resEnergy").textContent = Number(bill.energyCharge).toFixed(2);
        document.getElementById("resFixed").textContent = Number(bill.fixedCharge).toFixed(2);
        document.getElementById("resTotal").textContent = Number(bill.totalAmount).toFixed(2);

        // Display current timestamp on receipt
        const now = new Date();
        document.getElementById("billDate").textContent = `Generated: ${now.toLocaleDateString()} ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

        // Unhide receipt
        receiptSection.classList.remove("hidden");
        receiptSection.scrollIntoView({ behavior: "smooth" });
    }

    // Helper: Show error alert
    function showError(message) {
        errorBox.textContent = message;
        errorBox.classList.remove("hidden");
    }

    // Helper: Hide error alert
    function hideError() {
        errorBox.textContent = "";
        errorBox.classList.add("hidden");
    }
});
