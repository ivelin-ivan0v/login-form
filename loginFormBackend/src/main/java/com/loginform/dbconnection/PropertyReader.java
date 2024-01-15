package com.loginform.dbconnection;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private final  Properties properties;

    public PropertyReader() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
