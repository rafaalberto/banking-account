package com.api.account.integration.repository.impl;

import com.api.account.database.ConnectionFactory;
import com.api.account.database.DatabaseConnection;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;
import com.api.account.repository.BalanceDao;
import com.api.account.repository.impl.AccountDaoImpl;
import com.api.account.repository.impl.BalanceDaoImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static com.api.account.service.CalculationService.deposit;
import static com.api.account.service.CalculationService.withdraw;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;

public class BalanceDaoIntegrationTest {

    private AccountDao accountDao;
    private BalanceDao balanceDao;
    private TransactionContext transactionContext;

    @BeforeEach
    public void setUp () throws SQLException {
        DatabaseConnection.startup();
        accountDao = new AccountDaoImpl();
        balanceDao = new BalanceDaoImpl();

        Connection connection = ConnectionFactory.getConnection();
        transactionContext = new TransactionContextImpl(connection);
    }

    @Test
    public void shouldUpdateBalance() {
        Account accountInserted = accountDao.insert(new Account("Mary"));
        accountInserted.setBalance(new BigDecimal(2000));

        Account accountBalanceUpdated = balanceDao.updateBalance(accountInserted, transactionContext);

        assertThat(accountBalanceUpdated.getName()).isEqualTo("Mary");
        assertThat(accountBalanceUpdated.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(2000)));
    }

    @Test
    public void shouldUpdateBalanceByTransfer() {
        Account accountSender = accountDeposit("Rafael", new BigDecimal(1000));
        Account accountReceiver = accountDeposit("Mary", new BigDecimal(500));
        accountTransfer(accountSender, accountReceiver, new BigDecimal(200));
        verifyAccountsBalanceAfterTransfer(accountSender, accountReceiver);
    }

    private Account accountDeposit(String rafael, BigDecimal amount) {
        Account accountSender = accountDao.insert(new Account(rafael));
        accountSender.setBalance(deposit(accountSender.getBalance(), amount));
        accountSender = balanceDao.updateBalance(accountSender, transactionContext);
        return accountSender;
    }

    private void accountTransfer(Account accountSender, Account accountReceiver, BigDecimal amount) {
        accountSender.setBalance(withdraw(accountSender.getBalance(), amount));
        accountReceiver.setBalance(deposit(accountReceiver.getBalance(), amount));
        balanceDao.updateBalancesForTransfer(accountSender, accountReceiver);
    }

    private void verifyAccountsBalanceAfterTransfer(Account accountSender, Account accountReceiver) {
        accountSender = accountDao.findById(accountSender.getId());
        accountReceiver = accountDao.findById(accountReceiver.getId());

        assertThat(accountSender.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(800)));
        assertThat(accountReceiver.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(700)));
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (transactionContext instanceof TransactionContextImpl) {
            ((TransactionContextImpl) transactionContext).getConnection().close();
        }
        accountDao.deleteAll();
    }

}
