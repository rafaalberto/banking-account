package com.api.account.repository.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.model.Account;
import com.api.account.repository.BalanceDao;

import java.sql.*;

public class BalanceDaoImpl implements BalanceDao {

    @Override
    public Account updateBalance(Account account) {
        String sql = "update accounts set balance = ? where id = ?";
        try {
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setBigDecimal(1, account.getBalance());
                preparedStatement.setLong(2, account.getId());
                preparedStatement.execute();
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error to update", e);
        }
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

}
