package com.api.account.repository.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.exception.DataAccessException;
import com.api.account.model.Account;
import com.api.account.repository.BalanceDao;

import java.sql.*;

public class BalanceDaoImpl implements BalanceDao {

    @Override
    public Account updateBalance(Account account, TransactionContext transactionContext) {
        Connection connection = getConnection(transactionContext);
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setBigDecimal(1, account.getBalance());
                preparedStatement.setLong(2, account.getId());
                preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update balance for account: " + account.getId(), e);
        }
        return account;
    }

    @Override
    public void updateBalancesForTransfer(Account accountSender, Account accountReceiver) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String sql = "update accounts set balance = ? where id = ?";

                try (PreparedStatement preparedStatementSender = connection.prepareStatement(sql)) {
                    preparedStatementSender.setBigDecimal(1, accountSender.getBalance());
                    preparedStatementSender.setLong(2, accountSender.getId());
                    preparedStatementSender.execute();
                }

                try (PreparedStatement preparedStatementReceiver = connection.prepareStatement(sql)) {
                    preparedStatementReceiver.setBigDecimal(1, accountReceiver.getBalance());
                    preparedStatementReceiver.setLong(2, accountReceiver.getId());
                    preparedStatementReceiver.execute();
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Transaction failed and was rolled back", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection", e);
        }
    }

    private Connection getConnection(TransactionContext context) {
        if (context instanceof TransactionContextImpl) {
            return ((TransactionContextImpl) context).getConnection();
        }
        throw new IllegalArgumentException("Invalid transaction context");
    }

}
