/**
 * BankWebServer.java
 * 
 * Purpose:
 * Lightweight built-in HTTP Web Server for the Bank Management System.
 * Uses core Java standard library (com.sun.net.httpserver.HttpServer) with zero external frameworks.
 * Serves the static HTML/CSS/JS frontend on port 8080 and provides REST APIs for
 * Account Registration, Login, Deposit, Withdrawal, Transfer, and Statement Generation.
 * 
 * Target Audience: 2nd-Year Engineering Students learning Full-Stack Web Development in Core Java.
 */

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;

public class BankWebServer {
    private static final int PORT = 8080;
    private static final String DATA_FILE = "accounts.txt";
    private static Bank bank;
    private static File webUiDir;

    public static void main(String[] args) {
        try {
            // 1. Initialize data store
            System.out.println("=================================================");
            System.out.println("     STARTING APEX BANK HTTP WEB SERVER          ");
            System.out.println("=================================================");
            
            ArrayList<Account> loadedAccounts = FileHandler.loadAccounts(DATA_FILE);
            bank = new Bank(loadedAccounts);

            // 2. Locate the Web UI Directory (BankWebUI)
            locateWebUiDirectory();

            // 3. Create HTTP Server on port 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            // 4. Register REST API Handlers
            server.createContext("/api/register", new RegisterHandler());
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/account", new AccountDetailsHandler());
            server.createContext("/api/deposit", new DepositHandler());
            server.createContext("/api/withdraw", new WithdrawHandler());
            server.createContext("/api/transfer", new TransferHandler());
            server.createContext("/api/statement", new StatementHandler());

            // 5. Register Static File Handler (Root)
            server.createContext("/", new StaticFileHandler());

            // 6. Start the server
            server.start();
            System.out.println("\n🚀 Bank Web Server is running successfully!");
            System.out.println("🌐 Open your browser and visit: http://localhost:" + PORT);
            System.out.println("📁 Serving Web UI from: " + webUiDir.getAbsolutePath());
            System.out.println("⏹️  Press Ctrl + C in this terminal to stop the server.\n");

        } catch (IOException e) {
            System.err.println("[Server Error] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds the BankWebUI folder whether executed from BankJavaApp or project root.
     */
    private static void locateWebUiDirectory() {
        File[] candidatePaths = new File[] {
            new File("BankWebUI"),
            new File("../BankWebUI"),
            new File("BankManagementSystem/BankWebUI"),
            new File("../../BankWebUI")
        };

        for (File path : candidatePaths) {
            if (path.exists() && path.isDirectory()) {
                webUiDir = path;
                return;
            }
        }
        // Default fallback
        webUiDir = new File("BankWebUI");
    }

    // ==================== REST API HANDLERS ====================

    /**
     * Handler: POST /api/register
     */
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, jsonResponse(false, "Method not allowed. Use POST."));
                return;
            }

            Map<String, String> params = parseRequestBody(exchange);
            String accNo = params.get("accountNumber");
            String name = params.get("holderName");
            String pin = params.get("pin");
            double initialDeposit = parseDouble(params.get("initialDeposit"), 0.0);

            if (accNo == null || accNo.trim().isEmpty() || name == null || name.trim().isEmpty() || pin == null || pin.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Account number, name, and PIN are required."));
                return;
            }

            if (bank.findAccount(accNo) != null) {
                sendJsonResponse(exchange, 409, jsonResponse(false, "Account number '" + accNo + "' is already registered!"));
                return;
            }

            boolean created = bank.createAccount(accNo, name, pin, initialDeposit);
            if (created) {
                FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Account created successfully!\",\"accountNumber\":\"" + escapeJson(accNo) + "\",\"holderName\":\"" + escapeJson(name) + "\",\"balance\":" + initialDeposit + "}");
            } else {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Could not create account. Check inputs."));
            }
        }
    }

    /**
     * Handler: POST /api/login
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, jsonResponse(false, "Method not allowed. Use POST."));
                return;
            }

            Map<String, String> params = parseRequestBody(exchange);
            String accNo = params.get("accountNumber");
            String pin = params.get("pin");

            Account acc = bank.findAccount(accNo);
            if (acc == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Account number '" + accNo + "' not found."));
                return;
            }

            if (!acc.validatePin(pin)) {
                sendJsonResponse(exchange, 401, jsonResponse(false, "Invalid security PIN entered."));
                return;
            }

            String response = String.format(
                "{\"success\":true,\"message\":\"Login successful!\",\"accountNumber\":\"%s\",\"holderName\":\"%s\",\"balance\":%.2f}",
                escapeJson(acc.getAccountNumber()), escapeJson(acc.getHolderName()), acc.getBalance()
            );
            sendJsonResponse(exchange, 200, response);
        }
    }

    /**
     * Handler: GET /api/account?accountNumber=...&pin=...
     */
    static class AccountDetailsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String accNo = queryParams.get("accountNumber");
            String pin = queryParams.get("pin");

            Account acc = bank.findAccount(accNo);
            if (acc == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Account not found."));
                return;
            }

            if (pin != null && !acc.validatePin(pin)) {
                sendJsonResponse(exchange, 401, jsonResponse(false, "Incorrect PIN."));
                return;
            }

            String response = String.format(
                "{\"success\":true,\"accountNumber\":\"%s\",\"holderName\":\"%s\",\"balance\":%.2f}",
                escapeJson(acc.getAccountNumber()), escapeJson(acc.getHolderName()), acc.getBalance()
            );
            sendJsonResponse(exchange, 200, response);
        }
    }

    /**
     * Handler: POST /api/deposit
     */
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseRequestBody(exchange);
            String accNo = params.get("accountNumber");
            double amount = parseDouble(params.get("amount"), 0.0);

            if (amount <= 0) {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Deposit amount must be greater than zero."));
                return;
            }

            Account acc = bank.findAccount(accNo);
            if (acc == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Account not found."));
                return;
            }

            boolean success = bank.deposit(accNo, amount);
            if (success) {
                FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
                sendJsonResponse(exchange, 200, String.format(
                    "{\"success\":true,\"message\":\"Successfully deposited $%.2f\",\"balance\":%.2f}",
                    amount, acc.getBalance()
                ));
            } else {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Deposit failed."));
            }
        }
    }

    /**
     * Handler: POST /api/withdraw
     */
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseRequestBody(exchange);
            String accNo = params.get("accountNumber");
            String pin = params.get("pin");
            double amount = parseDouble(params.get("amount"), 0.0);

            Account acc = bank.findAccount(accNo);
            if (acc == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Account not found."));
                return;
            }

            if (!acc.validatePin(pin)) {
                sendJsonResponse(exchange, 401, jsonResponse(false, "Incorrect PIN. Withdrawal denied."));
                return;
            }

            if (amount <= 0) {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Withdrawal amount must be greater than zero."));
                return;
            }

            if (acc.getBalance() < amount) {
                sendJsonResponse(exchange, 400, jsonResponse(false, String.format("Insufficient funds! Balance: $%.2f", acc.getBalance())));
                return;
            }

            boolean success = bank.withdraw(accNo, pin, amount);
            if (success) {
                FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
                sendJsonResponse(exchange, 200, String.format(
                    "{\"success\":true,\"message\":\"Successfully withdrew $%.2f\",\"balance\":%.2f}",
                    amount, acc.getBalance()
                ));
            } else {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Withdrawal failed."));
            }
        }
    }

    /**
     * Handler: POST /api/transfer
     */
    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseRequestBody(exchange);
            String fromAcc = params.get("fromAccount");
            String pin = params.get("pin");
            String toAcc = params.get("toAccount");
            double amount = parseDouble(params.get("amount"), 0.0);

            if (fromAcc == null || toAcc == null || fromAcc.equalsIgnoreCase(toAcc)) {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Source and destination accounts must be different."));
                return;
            }

            Account sender = bank.findAccount(fromAcc);
            if (sender == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Sender account not found."));
                return;
            }

            if (!sender.validatePin(pin)) {
                sendJsonResponse(exchange, 401, jsonResponse(false, "Incorrect sender PIN!"));
                return;
            }

            Account recipient = bank.findAccount(toAcc);
            if (recipient == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Beneficiary account '" + toAcc + "' not found."));
                return;
            }

            if (amount <= 0) {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Transfer amount must be positive."));
                return;
            }

            if (sender.getBalance() < amount) {
                sendJsonResponse(exchange, 400, jsonResponse(false, String.format("Insufficient funds! Available: $%.2f", sender.getBalance())));
                return;
            }

            boolean success = bank.transfer(fromAcc, pin, toAcc, amount);
            if (success) {
                FileHandler.saveAccounts(bank.getAccounts(), DATA_FILE);
                sendJsonResponse(exchange, 200, String.format(
                    "{\"success\":true,\"message\":\"Transferred $%.2f to %s (Acc: %s) successfully!\",\"balance\":%.2f}",
                    amount, escapeJson(recipient.getHolderName()), escapeJson(recipient.getAccountNumber()), sender.getBalance()
                ));
            } else {
                sendJsonResponse(exchange, 400, jsonResponse(false, "Transfer failed."));
            }
        }
    }

    /**
     * Handler: GET /api/statement?accountNumber=...
     */
    static class StatementHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String accNo = queryParams.get("accountNumber");

            Account acc = bank.findAccount(accNo);
            if (acc == null) {
                sendJsonResponse(exchange, 404, jsonResponse(false, "Account not found."));
                return;
            }

            List<Transaction> txns = bank.getTransactionsForAccount(accNo);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,\"accountNumber\":\"").append(escapeJson(acc.getAccountNumber()))
              .append("\",\"balance\":").append(acc.getBalance())
              .append(",\"transactions\":[");

            for (int i = 0; i < txns.size(); i++) {
                Transaction t = txns.get(i);
                sb.append(String.format(
                    "{\"id\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"date\":\"%s\",\"remarks\":\"%s\"}",
                    escapeJson(t.getTransactionId()),
                    escapeJson(t.getType()),
                    t.getAmount(),
                    escapeJson(t.getDate()),
                    escapeJson(t.getRemarks())
                ));
                if (i < txns.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    // ==================== STATIC FILE HANDLER ====================

    /**
     * Serves HTML, CSS, JS, and asset files from the BankWebUI folder.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Normalize path: "/" -> "/index.html"
            if (path == null || path.equals("/") || path.trim().isEmpty()) {
                path = "/index.html";
            }

            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            File requestedFile = new File(webUiDir, path);

            if (!requestedFile.exists() || requestedFile.isDirectory()) {
                // Return 404
                String notFound = "<html><body><h1>404 Not Found</h1><p>The requested file <code>" + path + "</code> was not found.</p><a href='/'>Go to Home</a></body></html>";
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            // Detect MIME Type
            String mimeType = getMimeType(requestedFile.getName());
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");

            byte[] fileBytes = Files.readAllBytes(requestedFile.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private static String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "text/plain; charset=UTF-8";
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String jsonResponse(boolean success, String message) {
        return "{\"success\":" + success + ",\"message\":\"" + escapeJson(message) + "\"}";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        String body = baos.toString(StandardCharsets.UTF_8.name()).trim();
        Map<String, String> map = new HashMap<>();

        if (body.isEmpty()) return map;

        // If JSON format
        if (body.startsWith("{") && body.endsWith("}")) {
            String inner = body.substring(1, body.length() - 1);
            // Basic JSON key-value extraction for primitive fields
            String[] tokens = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String token : tokens) {
                int colonIdx = token.indexOf(':');
                if (colonIdx > 0) {
                    String key = token.substring(0, colonIdx).trim().replace("\"", "");
                    String val = token.substring(colonIdx + 1).trim();
                    if (val.startsWith("\"") && val.endsWith("\"")) {
                        val = val.substring(1, val.length() - 1);
                    }
                    map.put(key, val);
                }
            }
        } else {
            // Form URL Encoded
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                int eqIdx = pair.indexOf('=');
                if (eqIdx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, eqIdx), "UTF-8");
                    String val = URLDecoder.decode(pair.substring(eqIdx + 1), "UTF-8");
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;
        try {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int eqIdx = pair.indexOf('=');
                if (eqIdx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, eqIdx), "UTF-8");
                    String val = URLDecoder.decode(pair.substring(eqIdx + 1), "UTF-8");
                    map.put(key, val);
                }
            }
        } catch (Exception e) {
            // Ignore malformed query string
        }
        return map;
    }

    private static double parseDouble(String str, double def) {
        if (str == null) return def;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
