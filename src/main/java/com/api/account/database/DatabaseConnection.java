package com.api.account.database;

import com.api.account.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.api.account.database.ConnectionFactory.getConnection;

public class DatabaseConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String DROP_TABLE_ACCOUNTS = "DROP TABLE IF EXISTS accounts";

    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS accounts " +
            "(id bigint auto_increment NOT NULL, name VARCHAR(255) NOT NULL, balance DECIMAL(10,2) NOT NULL, PRIMARY KEY( id ))";

    public static void startup() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(DROP_TABLE_ACCOUNTS);
            statement.executeUpdate(CREATE_TABLE_ACCOUNTS);
        } catch (SQLException e) {
            LOGGER.error("Failed to connect to database", e);
            throw new DataAccessException("Database connection failed", e);
        }
    }

}
