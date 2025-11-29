package com.api.account.repository.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.exception.DataAccessException;
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
            throw new RuntimeException("Error to insert", e);
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
            throw new RuntimeException("Error to update", e);
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
            throw new RuntimeException("Error to delete", e);
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
            throw new RuntimeException("Error to delete", e);
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
            }
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException("Error to select", e);
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
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Error to find", e);
        }
    }

    public Account findByIdWithLock(Long id, TransactionContext context) {
        Connection connection = getConnection(context);
        String sql = "SELECT * FROM accounts WHERE id = ? FOR UPDATE";
        Account account = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                account = new Account();
                account.setId(resultSet.getLong("id"));
                account.setName(resultSet.getString("name"));
                account.setBalance(resultSet.getBigDecimal("balance"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find account with lock: " + id, e);
        }

        if (account == null) {
            throw new DataAccessException("Account not found with id: " + id);
        }
        return account;
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

    private Connection getConnection(TransactionContext context) {
        if (context instanceof TransactionContextImpl) {
            return ((TransactionContextImpl) context).getConnection();
        }
        throw new IllegalArgumentException("Invalid transaction context");
    }

}
