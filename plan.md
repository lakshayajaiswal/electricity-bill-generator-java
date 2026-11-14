# Revised Project Plan: Electricity Bill Generator & Connection Manager

## 1. Project Overview & Folder Structure
- **Student Name:** Lakshaya Jaiswal (Batch 9)
- **Topic:** Electricity Bill Generator & Connection Manager
- **Tech Stack:**
  - **Backend:** Java 17+ (Core Java only, built-in `com.sun.net.httpserver.HttpServer`, zero external libraries or dependencies)
  - **Frontend:** HTML5, CSS3, Vanilla JavaScript (Served directly by the Java backend)
  - **Architecture:** Monolithic single-server model (Java serves both frontend assets and POST API endpoint)

### Project Directory Layout:
```text
electricity-bill-manager/
│
├── .agents/skills/                   <-- All 4 configuration skills
│
├── backend/
│   └── Main.java                     <-- Single file containing the entire backend
│
├── frontend/
│   ├── index.html                    <-- Consumer entry form & bill receipt
│   ├── style.css                     <-- Clean styling & print receipt styles
│   └── script.js                     <-- Form event handler & POST fetch()
│
└── plan.md                           <-- Updated architecture plan
```

---

## 2. Key Architecture Decisions

1. **No External Dependencies & Single Java File:**
   - The entire backend is contained inside `Main.java`.
   - Uses built-in Java standard library (`com.sun.net.httpserver.*`, `java.io.*`, `java.nio.file.*`).
   - No Spring Boot, Node.js, Tomcat, Maven, or external JAR files.

2. **Frontend Served by Java Server (No CORS Needed):**
   - The Java `HttpServer` runs on `http://localhost:8080/`.
   - Visiting `http://localhost:8080/` serves `frontend/index.html`, `style.css`, and `script.js` directly.
   - Because both frontend and backend share the same origin (`http://localhost:8080`), **no CORS configuration is needed**.

3. **HTTP POST for Form Submission:**
   - The frontend collects consumer and consumption data and sends an **HTTP POST** request to `/calculate`.
   - Request payload is sent in the body (form-encoded or simple key-value/JSON format).

4. **Basic Connection Management (No Database):**
   - Basic in-memory or transactional connection verification (Consumer Name, Connection ID, Connection Category e.g., Domestic / Commercial).
   - Keeps code minimal and beginner-friendly without complex JDBC, SQL, or database setup.

---

## 3. The Four Essential Java Methods in `Main.java`

`Main.java` is kept strictly minimal with only **4 essential methods**:

| Method Name | Purpose | What to Explain to Your Teacher |
| :--- | :--- | :--- |
| `public static void main(String[] args)` | Starts the HTTP server on port 8080 and registers routes for serving frontend files (`/`) and processing bills (`/calculate`). | *"Starts Java's built-in HttpServer without external web servers and registers our endpoints."* |
| `private static void handleCalculate(HttpExchange exchange)` | Handles HTTP POST requests: reads request body, validates inputs, calls calculation methods, and writes back the bill response. | *"Acts as our controller: it receives form inputs via POST, validates them, and sends back the generated bill."* |
| `private static double calculateUnits(double watts, double hours, int days)` | Computes consumed electricity units (kWh) using the standard formula. | *"Calculates units: (Watts * Hours * Days) / 1000. All mathematical logic is kept in Java."* |
| `private static double calculateEnergyCharge(double units)` | Calculates energy charges using clear demonstration tariff rates + demonstration fixed charge. | *"Applies demo tariff slabs (e.g., ₹5.00/unit up to 100 units, ₹7.00/unit above 100 units) plus fixed meter charge."* |

*(Note: Serving static frontend files is handled cleanly in `main` using standard `Files.readAllBytes()` or a concise 3-line static file route).*

---

## 4. Pure Java Electricity Calculation Logic (Demonstration Rates)

All calculations occur strictly inside Java. **JavaScript does zero calculations.**

1. **Consumed Units Formula:**
   $$\text{Units (kWh)} = \frac{\text{Watts} \times \text{Hours/Day} \times \text{Days}}{1000}$$

2. **Demonstration Tariff Slabs:**
   - **First 100 Units:** ₹ 4.50 per unit
   - **Units above 100:** ₹ 6.50 per unit
   - **Fixed Monthly Connection Charge:** ₹ 50.00
   *(Explicitly stated as Demonstration Rates for academic presentation, not official utility board rates).*

3. **Input Validation (in Java):**
   - Watts must be $> 0$.
   - Usage hours per day must be between $0.1$ and $24.0$.
   - Billing days must be between $1$ and $365$.
   - Consumer Name and Connection ID must not be empty.
   - If validation fails, Java responds with an HTTP 400 and a clear error message.

---

## 5. Frontend-to-Backend Communication Flow

```text
[ Browser (http://localhost:8080) ]
        │
        │ 1. User fills: Name, Connection ID, Watts, Hours/Day, Days
        │ 2. User clicks "Generate Electricity Bill"
        │ 3. Vanilla JS sends HTTP POST to /calculate with form data
        ▼
[ Java HttpServer (Main.java) ]
        │
        │ 4. handleCalculate() reads POST body
        │ 5. Validates inputs (non-empty, positive numbers, hours <= 24)
        │ 6. calculateUnits() -> computes kWh
        │ 7. calculateEnergyCharge() -> computes demo slab charge + fixed fee
        │ 8. Sends HTTP 200 response with calculated bill values
        ▼
[ Browser UI ]
        │
        │ 9. script.js receives Java calculation results
        │ 10. Displays styled Bill Receipt with consumer details
        │ 11. "Print Bill" button allows printing receipt via window.print()
```

---

## 6. Teacher Viva / Presentation Preparation

- **Why core Java without frameworks?**
  *"To demonstrate fundamental Java programming, HTTP protocol handling, and socket/server concepts without hiding logic behind frameworks like Spring Boot."*
- **Why is CORS avoided?**
  *"Because the Java server hosts the static frontend files directly on port 8080, both client and server share the identical origin."*
- **Why POST instead of GET?**
  *"POST encapsulates the form data inside the HTTP request body rather than exposing user input in the URL query string."*
