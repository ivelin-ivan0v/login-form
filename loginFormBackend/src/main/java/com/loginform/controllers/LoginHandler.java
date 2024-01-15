package com.loginform.controllers;

import com.google.gson.Gson;
import com.loginform.dbconnection.DatabaseConnector;
import com.loginform.user.UserLoginDTO;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // Add CORS headers to allow any origin
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        headers.add("Access-Control-Allow-Credentials", "true");

        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            UserLoginDTO user = gson.fromJson(isr, UserLoginDTO.class);

            String response = validateUser(user) ? "{\"message\": \"Login Successful\"}" : "{\"error\": \"Invalid Credentials\"}";
            sendJsonResponse(exchange, 200, response);
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

//    private boolean validateUser(UserLoginDTO user) {
//        try (Connection conn = DatabaseConnector.getConnection()) {
//            String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND password = ?";
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                pstmt.setString(1, user.getEmail());
//                pstmt.setString(2, user.getPassword()); // Assuming plaintext passwords for simplicity
//
//                try (ResultSet rs = pstmt.executeQuery()) {
//                    return rs.next() && rs.getInt(1) > 0;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    private boolean validateUser(UserLoginDTO user) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getEmail());
                pstmt.setString(2, user.getPassword()); // Assuming plaintext passwords for simplicity

                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            if ("22001".equals(e.getSQLState())) {
                // SQLState "22001" typically indicates a data truncation error,
                // which might occur due to incorrect password length
                return false;
            }
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
}
