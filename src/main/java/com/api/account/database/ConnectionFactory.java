package com.api.account.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String DATABASE_URL = "jdbc:h2:~/test";
    private static final String DATABASE_USER = "sa";
    private static final String DATABASE_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }
}
