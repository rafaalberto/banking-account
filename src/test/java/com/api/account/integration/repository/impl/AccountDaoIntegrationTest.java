package com.api.account.integration.repository.impl;

import com.api.account.database.DatabaseConnection;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;
import com.api.account.repository.impl.AccountDaoImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountDaoIntegrationTest {

    private AccountDao accountDao;

    @BeforeEach
    public void setUp () {
        DatabaseConnection.startup();
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

    @Test
    public void shouldFindAll() {
        accountDao.insert(new Account("Rafael"));
        accountDao.insert(new Account("John"));
        accountDao.insert(new Account("Pedro"));
        List<Account> accounts = accountDao.findAll();
        assertThat(accounts.size()).isEqualTo(3);
    }

    @AfterEach
    public void tearDown() {
        accountDao.deleteAll();
    }

}
