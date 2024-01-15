package com.loginform.controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.loginform.dbconnection.DatabaseConnector;
import com.loginform.user.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class UsersController implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            headers.add("Access-Control-Allow-Credentials", "true");
            String method = exchange.getRequestMethod();

            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, 0); // OK
                return;
            }

            switch (method) {
                case "GET":
                    handleGetRequest(exchange);
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "PUT":
                    handlePutRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\": \"Unsupported method\"}"); // Method Not Allowed
                    return;
            }

        } catch (IOException e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}"); // Internal Server Error
        }
    }


    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        sendResponse(exchange, statusCode, jsonResponse);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }


    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            String email = extractQueryParameter(exchange, "email");
            if (email == null || email.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Missing email parameter\"}");
                return;
            }

            try (Connection conn = DatabaseConnector.getConnection()) {
                String sql = "SELECT first_name, last_name, email FROM users WHERE email = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, email);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String userResponse = "{\"message\": \"User Found\", \"user\": {\"first_name\": \"" +
                                    rs.getString("first_name") + "\", \"last_name\": \"" +
                                    rs.getString("last_name") + "\", \"email\": \"" +
                                    rs.getString("email") + "\"}}";
                            sendResponse(exchange, 200, userResponse);
                        } else {
                            sendResponse(exchange, 404, "{\"error\": \"User Not Found\"}");
                        }
                    }
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: Database Error\"}");
            }
        } catch (IOException e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: IO Error\"}");
        }
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        try {
            String email = extractQueryParameter(exchange, "email");
            if (email == null || email.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Missing email parameter\"}");
                return;
            }

            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            User user = gson.fromJson(requestBody, User.class);

            if (user == null || user.getFirstName() == null || user.getLastName() == null) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user data\"}");
                return;
            }

            try (Connection conn = DatabaseConnector.getConnection()) {
                String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE email = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, user.getFirstName());
                    pstmt.setString(2, user.getLastName());
                    pstmt.setString(3, email);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        sendResponse(exchange, 200, "{\"message\": \"User Updated Successfully\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"User Not Found\"}");
                    }
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: Database Error\"}");
            }
        } catch (IOException e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: IO Error\"}");
        }
    }


    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            User user = gson.fromJson(requestBody, User.class);

            if (user == null || user.getEmail() == null || user.getFirstName() == null || user.getLastName() == null) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user data\"}");
                return;
            }

            try (Connection conn = DatabaseConnector.getConnection()) {
                String sql = "INSERT INTO users (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, user.getFirstName());
                    pstmt.setString(2, user.getLastName());
                    pstmt.setString(3, user.getEmail());
                    pstmt.setString(4, user.getPassword());

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        sendResponse(exchange, 201, "{\"message\": \"User Created Successfully\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: Unable to create user\"}");
                    }
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\": \"Database Error\"}");
            }
        } catch (IOException e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: IO Error\"}");
        }
    }


    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        try {
            String email = extractQueryParameter(exchange, "email");
            if (email == null || email.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Missing email parameter\"}");
                return;
            }

            try (Connection conn = DatabaseConnector.getConnection()) {
                String sql = "DELETE FROM users WHERE email = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, email);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        sendResponse(exchange, 200, "{\"message\": \"User Deleted Successfully\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"User Not Found\"}");
                    }
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: Database Error\"}");
            }
        } catch (IOException e) {
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: IO Error\"}");
        }
    }

    private String extractQueryParameter(HttpExchange exchange, String parameterName) {
        // Extract and return the value of the specified query parameter from the exchange
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(parameterName)) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
}
