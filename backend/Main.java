import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        // Read dynamic port assigned by Render, defaulting to 8080 for local execution
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.isEmpty()) ? Integer.parseInt(portEnv) : 8080;

        // Bind to 0.0.0.0 for cloud container networking
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        // Securely serve static frontend files
        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath().equals("/") ? "/index.html" : ex.getRequestURI().getPath();

            // Resolve frontend folder reliably in both local and container environments
            Path baseDir = Files.exists(Path.of("frontend")) ? Path.of("frontend")
                    : Files.exists(Path.of("..", "frontend")) ? Path.of("..", "frontend")
                    : Path.of("electricity-bill-manager", "frontend");
            baseDir = baseDir.toAbsolutePath().normalize();

            String rel = path.startsWith("/") ? path.substring(1) : path;
            Path file = baseDir.resolve(rel).normalize();

            if (!file.startsWith(baseDir)) {
                send(ex, 403, "text/plain", "Access Denied");
            } else if (Files.exists(file) && !Files.isDirectory(file)) {
                String type = path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "application/javascript" : "text/html";
                send(ex, 200, type, Files.readAllBytes(file));
            } else {
                send(ex, 404, "text/plain", "File Not Found");
            }
        });

        // Register calculation endpoint
        server.createContext("/calculate", Main::handleCalculate);
        server.start();
        System.out.println("Server running on port " + port);
    }

    private static void handleCalculate(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "text/plain", "Method Not Allowed");
            return;
        }

        // Read and parse form inputs
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] p = pair.split("=", 2);
            if (p.length == 2) {
                params.put(URLDecoder.decode(p[0], StandardCharsets.UTF_8).trim(), URLDecoder.decode(p[1], StandardCharsets.UTF_8).trim());
            }
        }

        String name = params.getOrDefault("consumerName", "");
        String id = params.getOrDefault("connectionId", "");
        String type = params.getOrDefault("connectionType", "");
        boolean validCategory = "Domestic".equalsIgnoreCase(type) || "Commercial".equalsIgnoreCase(type);

        double watts;
        double hours;
        int days;

        // Parse numeric values with dedicated try-catch block
        try {
            watts = Double.parseDouble(params.getOrDefault("watts", "0"));
            hours = Double.parseDouble(params.getOrDefault("hours", "0"));
            days = Integer.parseInt(params.getOrDefault("days", "0"));
        } catch (NumberFormatException e) {
            send(ex, 400, "application/json", "{\"error\":\"Invalid numeric input values.\"}");
            return;
        }

        // Input validation in Java
        if (name.isEmpty() || id.isEmpty() || !validCategory ||
            !Double.isFinite(watts) || watts <= 0 ||
            !Double.isFinite(hours) || hours <= 0 || hours > 24 || days <= 0 || days > 365) {
            send(ex, 400, "application/json", "{\"error\":\"Invalid inputs: Check numbers, ranges, and connection type (Domestic/Commercial).\"}");
            return;
        }

        // Perform calculation logic
        double units = calculateUnits(watts, hours, days);
        double energyCharge = calculateEnergyCharge(units);
        double fixedCharge = 50.00; // Demonstration fixed charge
        double total = energyCharge + fixedCharge;
        String category = "Commercial".equalsIgnoreCase(type) ? "Commercial" : "Domestic";

        // Construct clean JSON response rounded to 2 decimal places
        String json = "{"
            + "\"consumerName\":\"" + escapeJson(name) + "\","
            + "\"connectionId\":\"" + escapeJson(id) + "\","
            + "\"connectionType\":\"" + escapeJson(category) + "\","
            + String.format(Locale.US, "\"units\":%.2f,", units)
            + String.format(Locale.US, "\"energyCharge\":%.2f,", energyCharge)
            + String.format(Locale.US, "\"fixedCharge\":%.2f,", fixedCharge)
            + String.format(Locale.US, "\"totalAmount\":%.2f", total)
            + "}";

        send(ex, 200, "application/json", json);
    }

    private static double calculateUnits(double watts, double hours, int days) {
        return (watts * hours * days) / 1000.0;
    }

    private static double calculateEnergyCharge(double units) {
        if (units <= 100.0) {
            return units * 4.50;
        } else {
            return (100.0 * 4.50) + ((units - 100.0) * 6.50);
        }
    }

    // Helper: Unified HTTP response handler for byte arrays
    private static void send(HttpExchange ex, int code, String type, byte[] bytes) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes == null || bytes.length == 0 ? -1 : bytes.length);
        if (bytes != null && bytes.length > 0) {
            ex.getResponseBody().write(bytes);
        }
        ex.close();
    }

    // Helper: Overloaded HTTP response handler for String text
    private static void send(HttpExchange ex, int code, String type, String text) throws IOException {
        byte[] bytes = (text == null) ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        send(ex, code, type, bytes);
    }

    // Helper: Escape JSON special characters to prevent broken syntax
    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String escape(String s) {
        return escapeJson(s);
    }
}
