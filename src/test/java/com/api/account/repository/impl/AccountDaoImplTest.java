package com.api.account.repository.impl;

import com.api.account.database.DatabaseConnection;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.api.account.service.CalculationService.deposit;
import static com.api.account.service.CalculationService.withdraw;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountDaoImplTest {

    private AccountDao accountDao;

    @BeforeEach
    public void setUp () {
        DatabaseConnection.createTables();
        accountDao = new AccountDaoImpl();
    }

    @Test
    public void shouldInsert() {
        Account accountInserted = accountDao.insert(new Account("Rafael"));
        accountInserted = accountDao.findById(accountInserted.getId());

        assertThat(accountInserted.getId()).isNotNull();
        assertThat(accountInserted.getName()).isEqualTo("Rafael");
    }

    @Test
    public void shouldUpdate() {
        Account accountInserted = accountDao.insert(new Account("Mary"));
        Account accountUpdated = accountDao.update(new Account(accountInserted.getId(),"Rafael"));

        assertThat(accountUpdated.getName()).isEqualTo("Rafael");
    }

    @Test
    public void shouldDelete() {
        Account accountInserted = accountDao.insert(new Account("Rafael"));
        Account accountFound = accountDao.findById(accountInserted.getId());
        accountDao.delete(accountFound.getId());
        Account accountDeleted = accountDao.findById(accountFound.getId());
        assertThat(accountDeleted).isNull();
    }

    /* Unit Test to findById doesn't need to implement because this one is has used in the previous tests */

    @Test
    public void shouldFindAll() {
        accountDao.insert(new Account("Rafael"));
        accountDao.insert(new Account("John"));
        accountDao.insert(new Account("Pedro"));
        List<Account> accounts = accountDao.findAll();
        assertThat(accounts.size()).isEqualTo(3);
    }

    @Test
    public void shouldUpdateBalance() {
        Account accountInserted = accountDao.insert(new Account("Mary"));
        accountInserted.setBalance(new BigDecimal(2000));

        Account accountBalanceUpdated = accountDao.updateBalance(accountInserted);

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
        accountSender = accountDao.update(accountSender);
        return accountSender;
    }

    private void accountTransfer(Account accountSender, Account accountReceiver, BigDecimal amount) {
        accountSender.setBalance(withdraw(accountSender.getBalance(), amount));
        accountReceiver.setBalance(deposit(accountReceiver.getBalance(), amount));
        accountDao.updateBalanceByTransfer(accountSender, accountReceiver);
    }

    private void verifyAccountsBalanceAfterTransfer(Account accountSender, Account accountReceiver) {
        accountSender = accountDao.findById(accountSender.getId());
        accountReceiver = accountDao.findById(accountReceiver.getId());

        assertThat(accountSender.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(800)));
        assertThat(accountReceiver.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(700)));
    }

    @AfterEach
    public void tearDown() {
        accountDao.deleteAll();
    }

}
