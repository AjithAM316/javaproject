package com.library.web;

import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.User;
import com.library.service.LibraryService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiHandler implements HttpHandler {

    private final LibraryService service;

    public ApiHandler(LibraryService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        // CORS Headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String response = "";
            if ("POST".equals(method) && path.endsWith("/login")) {
                response = handleLogin(exchange);
            } else if ("POST".equals(method) && path.endsWith("/register")) {
                response = handleRegister(exchange);
            } else if ("GET".equals(method) && path.endsWith("/books")) {
                response = handleGetBooks();
            } else if ("POST".equals(method) && path.endsWith("/issue")) {
                response = handleIssueBook(exchange);
            } else if ("POST".equals(method) && path.endsWith("/return")) {
                response = handleReturnBook(exchange);
            } else if ("GET".equals(method) && path.contains("/history")) {
                response = handleHistory(exchange);
            } else if ("GET".equals(method) && path.endsWith("/admin/issues/all")) {
                response = handleAdminIssues();
            } else {
                sendError(exchange, 404, "Unknown API endpoint");
                return;
            }
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendError(exchange, 400, e.getMessage());
        }
    }

    private String handleLogin(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);
        String username = extractJsonField(body, "username");
        String password = extractJsonField(body, "password");
        
        User user = service.login(username, password);
        return String.format("{\"success\":true, \"userId\":%d, \"role\":\"%s\", \"fullName\":\"%s\"}", 
            user.getUserId(), user.getRole().name(), user.getFullName());
    }

    private String handleRegister(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);
        String username = extractJsonField(body, "username");
        String password = extractJsonField(body, "password");
        String fullName = extractJsonField(body, "fullName");
        String email = extractJsonField(body, "email");
        String phone = extractJsonField(body, "phone");
        
        try {
            service.registerStudent(username, password, fullName, email, phone);
            return "{\"success\":true}";
        } catch (IllegalArgumentException e) {
            return String.format("{\"success\":false, \"error\":\"%s\"}", e.getMessage().replace("\"", "\\\""));
        }
    }

    private String handleGetBooks() throws Exception {
        List<Book> books = service.getAllBooks();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            String title = b.getTitle() != null ? b.getTitle().replace("\"", "\\\"").replace("\n", "\\n") : "";
            String author = b.getAuthor() != null ? b.getAuthor().replace("\"", "\\\"").replace("\n", "\\n") : "";
            String genre = b.getGenre() != null ? b.getGenre().replace("\"", "\\\"").replace("\n", "\\n") : "Uncategorized";
            String desc = b.getDescription() != null ? b.getDescription().replace("\"", "\\\"").replace("\n", "\\n") : "No description available.";
            
            json.append(String.format("{\"id\":%d, \"title\":\"%s\", \"author\":\"%s\", \"isbn\":\"%s\", \"genre\":\"%s\", \"description\":\"%s\", \"available\":%d, \"total\":%d}", 
                b.getBookId(), title, author, b.getIsbn(), genre, desc, b.getAvailableCopies(), b.getTotalCopies()));
            if (i < books.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private String handleIssueBook(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);
        int bookId = Integer.parseInt(extractJsonField(body, "bookId"));
        int userId = Integer.parseInt(extractJsonField(body, "userId"));
        String msg = service.issueBook(bookId, userId);
        return String.format("{\"success\":true, \"message\":\"%s\"}", msg);
    }
    
    private String handleReturnBook(HttpExchange exchange) throws Exception {
        String body = readBody(exchange);
        int bookId = Integer.parseInt(extractJsonField(body, "bookId"));
        int userId = Integer.parseInt(extractJsonField(body, "userId"));
        String msg = service.returnBook(bookId, userId);
        return String.format("{\"success\":true, \"message\":\"%s\"}", msg);
    }

    private String handleHistory(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery(); // e.g. "userId=1"
        int userId = Integer.parseInt(query.split("=")[1]);
        List<BookIssue> issues = service.getBorrowingHistory(userId);
        return serializeIssuesList(issues);
    }
    
    private String handleAdminIssues() throws Exception {
        List<BookIssue> issues = service.getAllActiveIssues();
        return serializeIssuesList(issues);
    }
    
    private String serializeIssuesList(List<BookIssue> issues) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < issues.size(); i++) {
            BookIssue is = issues.get(i);
            json.append(String.format("{\"issueId\":%d, \"bookId\":%d, \"userId\":%d, \"issueDate\":\"%s\", \"dueDate\":\"%s\", \"fine\":%.2f, \"status\":\"%s\"}", 
                is.getIssueId(), is.getBookId(), is.getUserId(), is.getIssueDate(), is.getDueDate(), is.calculateFine(), is.getStatus().name()));
            if (i < issues.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = String.format("{\"success\":false, \"error\":\"%s\"}", message.replace("\"", "\\\""));
        sendResponse(exchange, statusCode, json);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        // Fallback for non-string fields like numbers
        search = "\"" + field + "\":";
        start = json.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        return "";
    }
}
