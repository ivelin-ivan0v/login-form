package com.loginform;

import com.loginform.controllers.LoginHandler;
import com.loginform.dbconnection.DatabaseInitializer;
import com.sun.net.httpserver.HttpServer;
import com.loginform.controllers.UsersController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {

        DatabaseInitializer.initializeDatabase();

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/login", new LoginHandler());
        server.createContext("/users", new UsersController());
        server.setExecutor(null);
        server.start();
        System.out.println("SERVER RUNNING");

    }
}
