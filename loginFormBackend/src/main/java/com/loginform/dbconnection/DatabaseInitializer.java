package com.loginform.dbconnection;

import com.loginform.dbconnection.DatabaseConnector;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS users (" +
                        "email VARCHAR(255) PRIMARY KEY," +
                        "first_name VARCHAR(255) NOT NULL," +
                        "last_name VARCHAR(255) NOT NULL," +
                        "password VARCHAR(255) NOT NULL" +
                        ")";
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
