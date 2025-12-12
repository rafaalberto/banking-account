package com.api.account.repository.impl;

import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.exception.DataAccessException;
import com.api.account.model.Account;
import com.api.account.repository.BalanceDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BalanceDaoImpl implements BalanceDao {

  @Override
  public Account updateBalance(final Account account, final TransactionContext transactionContext) {
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
  public void updateBalancesForTransfer(
      final Account accountSender,
      final Account accountReceiver,
      final TransactionContext transactionContext) {
    Connection connection = getConnection(transactionContext);
    String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

    try (PreparedStatement preparedStatementSender = connection.prepareStatement(sql)) {
      preparedStatementSender.setBigDecimal(1, accountSender.getBalance());
      preparedStatementSender.setLong(2, accountSender.getId());
      preparedStatementSender.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException("Failed to update sender balance: " + accountSender.getId(), e);
    }

    try (PreparedStatement preparedStatementReceiver = connection.prepareStatement(sql)) {
      preparedStatementReceiver.setBigDecimal(1, accountReceiver.getBalance());
      preparedStatementReceiver.setLong(2, accountReceiver.getId());
      preparedStatementReceiver.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException(
          "Failed to update receiver balance: " + accountReceiver.getId(), e);
    }
  }

  private Connection getConnection(final TransactionContext context) {
    if (context instanceof TransactionContextImpl) {
      return ((TransactionContextImpl) context).getConnection();
    }
    throw new IllegalArgumentException("Invalid transaction context");
  }
}
