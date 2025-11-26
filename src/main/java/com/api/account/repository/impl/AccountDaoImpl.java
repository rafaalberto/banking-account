package com.api.account.repository.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements AccountDao {

    public Account insert(Account account) {
        String sql = "insert into accounts (name, balance) values (?, ?)";
        try {
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, account.getName());
                preparedStatement.setBigDecimal(2, account.getBalance());

                var rowsAffected = preparedStatement.executeUpdate();

                account.setId(getGeneratedId(preparedStatement, rowsAffected));

            }

            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account update(Account account) {
        String sql = "update accounts set name = ? where id = ?";
        try {
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, account.getName());
                preparedStatement.setLong(2, account.getId());
                preparedStatement.execute();
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Long id) {
        try {
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("delete from accounts where id = ?")) {
                preparedStatement.setLong(1, id);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Created in order to use in unit test */
    public void deleteAll() {
        try {
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("delete from accounts")) {
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Account> findAll() {
        String sql = "select * from accounts";
        try {
            List<Account> accounts = new ArrayList<>();
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Account account = new Account();
                    account.setId(resultSet.getLong("id"));
                    account.setName(resultSet.getString("name"));
                    account.setBalance(resultSet.getBigDecimal("balance"));
                    accounts.add(account);
                }
                resultSet.close();
            }
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account findById(Long id) {
        String sql = "select * from accounts where id = ?";
        try {
            Account account = null;
            try (Connection connection = ConnectionFactory.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    account = new Account();
                    account.setId(resultSet.getLong("id"));
                    account.setName(resultSet.getString("name"));
                    account.setBalance(resultSet.getBigDecimal("balance"));
                }
                resultSet.close();
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* used in deposit and withdraw transactions */
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
            throw new RuntimeException(e);
        }
    }

    /* used in transfer operations in order to treat database transactions */
    @Override
    public void updateBalanceByTransfer(Account accountSender, Account accountReceiver) {
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

    private Long getGeneratedId(PreparedStatement preparedStatement, int rowsAffected) throws SQLException {
        if (rowsAffected > BigInteger.ZERO.intValue()) {
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Operation failed no ID obtained");
                }
            }
        }
        return null;
    }

}
