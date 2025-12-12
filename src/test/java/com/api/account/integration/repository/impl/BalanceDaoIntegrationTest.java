package com.api.account.integration.repository.impl;

import static com.api.account.service.CalculationService.deposit;
import static com.api.account.service.CalculationService.withdraw;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;

import com.api.account.database.ConnectionFactory;
import com.api.account.database.DatabaseConnection;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;
import com.api.account.repository.BalanceDao;
import com.api.account.repository.impl.AccountDaoImpl;
import com.api.account.repository.impl.BalanceDaoImpl;
import com.api.account.unit.utils.TestDatabaseUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BalanceDaoIntegrationTest {

  private AccountDao accountDao;
  private BalanceDao balanceDao;
  private TransactionContext transactionContext;

  @BeforeEach
  public void setUp() throws SQLException {
    DatabaseConnection.startup();
    accountDao = new AccountDaoImpl();
    balanceDao = new BalanceDaoImpl();

    Connection connection = ConnectionFactory.getConnection();
    transactionContext = new TransactionContextImpl(connection);
  }

  @Test
  public void shouldUpdateBalance() {
    Account accountInserted = accountDao.insert(new Account("Mary"));
    accountInserted = accountInserted.withBalance(new BigDecimal(2000));

    Account accountBalanceUpdated = balanceDao.updateBalance(accountInserted, transactionContext);

    assertThat(accountBalanceUpdated.getName()).isEqualTo("Mary");
    assertThat(accountBalanceUpdated.getBalance())
        .isEqualTo(convertTwoDecimalPlace(new BigDecimal(2000)));
  }

  @Test
  public void shouldUpdateBalanceByTransfer() {
    Account accountSender = accountDeposit("Rafael", new BigDecimal(1000));
    Account accountReceiver = accountDeposit("Mary", new BigDecimal(500));
    accountTransfer(accountSender, accountReceiver, new BigDecimal(200));
    verifyAccountsBalanceAfterTransfer(accountSender, accountReceiver);
  }

  private Account accountDeposit(final String account, final BigDecimal amount) {
    Account accountSender = accountDao.insert(new Account(account));
    accountSender = accountSender.withBalance(deposit(accountSender.getBalance(), amount));
    accountSender = balanceDao.updateBalance(accountSender, transactionContext);
    return accountSender;
  }

  private void accountTransfer(
      final Account accountSender, final Account accountReceiver, final BigDecimal amount) {
    var sender = accountSender.withBalance(withdraw(accountSender.getBalance(), amount));
    var receiver = accountReceiver.withBalance(deposit(accountReceiver.getBalance(), amount));
    balanceDao.updateBalancesForTransfer(sender, receiver, transactionContext);
  }

  private void verifyAccountsBalanceAfterTransfer(
      final Account accountSender, final Account accountReceiver) {
    var sender = accountDao.findById(accountSender.getId());
    var receiver = accountDao.findById(accountReceiver.getId());

    assertThat(sender.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(800)));
    assertThat(receiver.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(700)));
  }

  @AfterEach
  public void tearDown() throws SQLException {
    if (transactionContext instanceof TransactionContextImpl) {
      ((TransactionContextImpl) transactionContext).getConnection().close();
    }
    TestDatabaseUtils.deleteAllAccounts();
  }
}
