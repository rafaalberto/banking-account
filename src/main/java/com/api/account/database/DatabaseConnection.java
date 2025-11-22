package com.api.account.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String DROP_TABLE_ACCOUNTS = "DROP TABLE IF EXISTS accounts";

    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS accounts " +
            "(id bigint auto_increment NOT NULL, name VARCHAR(255) NOT NULL, balance DECIMAL(10,2) NOT NULL, PRIMARY KEY( id ))";

    public static void connect() {
        try {
            Connection connection = ConnectionFactory.getConnection();
            LOGGER.info("Database connected");
            connection.close();
            LOGGER.info("Connection closed");
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createTables() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = ConnectionFactory.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(DROP_TABLE_ACCOUNTS);
            statement.executeUpdate(CREATE_TABLE_ACCOUNTS);

            LOGGER.info("Tables created");

        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }
    }
}
