package com.api.account.repository.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements AccountDao {

    private Connection connection;

    public AccountDaoImpl() {
        this.connection = ConnectionFactory.getConnection();
    }

    public Account insert(Account account) {
        String sql = "insert into accounts (name, balance) values (?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, account.getName());
            preparedStatement.setBigDecimal(2, account.getBalance());

            var rowsAffected = preparedStatement.executeUpdate();

            account.setId(getGeneratedId(preparedStatement, rowsAffected));

            preparedStatement.close();

            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account update(Account account) {
        String sql = "update accounts set name = ? where id = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, account.getName());
            preparedStatement.setLong(2, account.getId());
            preparedStatement.execute();
            preparedStatement.close();
            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Long id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from accounts where id = ?");
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Created in order to use in unit test */
    public void deleteAll() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("delete from accounts");
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Account> findAll() {
        String sql = "select * from accounts";
        try {
            List<Account> accounts = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Account account = new Account();
                account.setId(resultSet.getLong("id"));
                account.setName(resultSet.getString("name"));
                account.setBalance(resultSet.getBigDecimal("balance"));
                accounts.add(account);
            }
            resultSet.close();
            preparedStatement.close();
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account findById(Long id) {
        String sql = "select * from accounts where id = ?";
        try {
            Account account = null;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                account = new Account();
                account.setId(resultSet.getLong("id"));
                account.setName(resultSet.getString("name"));
                account.setBalance(resultSet.getBigDecimal("balance"));
            }
            resultSet.close();
            preparedStatement.close();
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
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBigDecimal(1, account.getBalance());
            preparedStatement.setLong(2, account.getId());
            preparedStatement.execute();
            preparedStatement.close();
            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* used in transfer operations in order to treat database transactions */
    @Override
    public void updateBalanceByTransfer(Account accountSender, Account accountReceiver) {
        try {
            connection.setAutoCommit(false);

            String sql = "update accounts set balance = ? where id = ?";

            PreparedStatement preparedStatementSender = connection.prepareStatement(sql);
            preparedStatementSender.setBigDecimal(1, accountSender.getBalance());
            preparedStatementSender.setLong(2, accountSender.getId());
            preparedStatementSender.execute();
            preparedStatementSender.close();

            PreparedStatement preparedStatementReceiver = connection.prepareStatement(sql);
            preparedStatementReceiver.setBigDecimal(1, accountReceiver.getBalance());
            preparedStatementReceiver.setLong(2, accountReceiver.getId());
            preparedStatementReceiver.execute();
            preparedStatementReceiver.close();

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Long getGeneratedId(PreparedStatement preparedStatement, int rowsAffected) throws SQLException {
        if(rowsAffected > BigInteger.ZERO.intValue()){
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
