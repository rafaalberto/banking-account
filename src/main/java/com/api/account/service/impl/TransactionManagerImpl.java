package com.api.account.service.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.exception.DataAccessException;
import com.api.account.exception.TransactionException;
import com.api.account.service.TransactionManager;
import com.api.account.service.TransactionOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database transactions and connection lifecycle.
 * Infrastructure concern - separated from business logic.
 */
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

    @Override
    public <T> T executeInTransaction(TransactionOperation<T> operation) {
        Connection connection = null;
        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false);

            TransactionContext context = new TransactionContextImpl(connection);
            T result = operation.execute(context);

            connection.commit();
            LOGGER.debug("Transaction committed successfully");
            return result;

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Transaction rolled back due to error", e);
                } catch (SQLException rollbackException) {
                    LOGGER.error("Failed to rollback transaction", rollbackException);
                    throw new DataAccessException("Failed to rollback transaction", rollbackException);
                }
            }
            throw new DataAccessException("Transaction failed", e);

        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                    LOGGER.warn("Transaction rolled back due to business exception", e);
                } catch (SQLException rollbackException) {
                    LOGGER.error("Failed to rollback transaction", rollbackException);
                }
            }

            // Re-throw business exceptions as-is
            if (e instanceof TransactionException) {
                throw e;
            }
            throw new TransactionException("Transaction failed", e);

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Failed to close connection", e);
                }
            }
        }
    }
}