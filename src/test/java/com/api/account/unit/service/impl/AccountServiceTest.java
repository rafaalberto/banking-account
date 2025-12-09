package com.api.account.unit.service.impl;

import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;
import com.api.account.service.AccountService;
import com.api.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    private Account account;

    @Mock
    private AccountDao accountDao;

    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        account = new Account(1L, "Rafael");
        accountService = new AccountServiceImpl(accountDao);
    }

    @Test
    public void shouldCreateAccountSuccessfully() {
        Account accountToCreate = new Account( "Rafael");
        when(accountDao.insert(accountToCreate)).thenReturn(account);
        Account accountCreated = accountService.save(accountToCreate);

        assertThat(accountCreated.getId()).isEqualTo(1L);
        assertThat(accountCreated.getName()).isEqualTo("Rafael");
        assertThat(accountCreated.getBalance()).isEqualTo(convertTwoDecimalPlace(BigDecimal.ZERO));
    }

    @Test
    public void shouldDenyCreateAccountIfNameNotInformed() {
        Account accountToCreate = new Account( " ");

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                accountService.save(accountToCreate)).withMessage("Name must be informed");
    }

    @Test
    public void shouldDenyCreateAccountIfBalanceGreaterThanZero() {
        Account accountToCreate = new Account( "Rafael");
        final Account accountWithBalance = accountToCreate.withBalance(convertTwoDecimalPlace(new BigDecimal(3000)));

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                accountService.save(accountWithBalance)).withMessage("Balance is not allowed to be saved for this operation");
    }

    @Test
    public void shouldUpdateAccountSuccessfullyNoBalance() {
        Account accountToUpdate = new Account(1L, "John");
        when(accountDao.findById(1L)).thenReturn(accountToUpdate);
        when(accountDao.update(accountToUpdate)).thenReturn(accountToUpdate);
        Account accountUpdated = accountService.save(accountToUpdate);

        assertThat(accountUpdated.getId()).isEqualTo(1L);
        assertThat(accountUpdated.getName()).isEqualTo("John");
        assertThat(accountUpdated.getBalance()).isEqualTo(convertTwoDecimalPlace(BigDecimal.ZERO));
    }

    @Test
    public void shouldUpdateAccountSuccessfullyWithBalance() {

        /* This account already has 3000 in balance */
        account = account.withBalance(convertTwoDecimalPlace(new BigDecimal(3000)));
        when(accountDao.findById(1L)).thenReturn(account);

        /* even if we put the balance to update, the app will not do that,
        because only transactions operations are allowed to do it */
        Account accountToUpdate = new Account(1L, "John");
        accountToUpdate = accountToUpdate.withBalance(new BigDecimal(5000));

        Account accountUpdated = new Account(1L, "John");
        accountUpdated = accountUpdated.withBalance(convertTwoDecimalPlace(new BigDecimal(3000)));
        when(accountDao.update(accountToUpdate)).thenReturn(accountUpdated);
        when(accountDao.findById(1L)).thenReturn(accountUpdated);

        accountUpdated = accountService.save(accountToUpdate);

        assertThat(accountUpdated.getId()).isEqualTo(1L);
        assertThat(accountUpdated.getName()).isEqualTo("John");
        assertThat(accountUpdated.getBalance()).isEqualTo(convertTwoDecimalPlace(new BigDecimal(3000)));
    }

    @Test
    public void shouldDenyUpdateAccountIfNameNotInformed() {
        Account accountToUpdate = new Account(1L, "  ");

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                accountService.save(accountToUpdate)).withMessage("Name must be informed");
    }

    @Test
    public void shouldDenyDeleteAccountIfNotFound() {
        when(accountDao.findById(1L)).thenReturn(null);
        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                accountService.delete(1L)).withMessage("Account not found");
    }

    @Test
    public void shouldFindAccountById() {
        Account accountFound = new Account(1L, "Rafael");
        when(accountDao.findById(accountFound.getId())).thenReturn(accountFound);
        accountFound = accountService.findById(accountFound.getId());
        assertThat(accountFound.getName()).isEqualTo("Rafael");
    }

    @Test
    public void shouldNotFindAccountById() {
        when(accountDao.findById(1L)).thenReturn(null);
        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                accountService.findById(1L)).withMessage("Account not found");
    }

    @Test
    public void shouldFindAccounts() {
        List<Account> accountsFound = new ArrayList<>();
        accountsFound.add(new Account(1L, "Rafael"));
        accountsFound.add(new Account(2L, "Mary"));
        accountsFound.add(new Account(3L, "Pedro"));
        when(accountDao.findAll()).thenReturn(accountsFound);
        accountsFound = accountService.findAll();
        assertThat(accountsFound.size()).isEqualTo(3);
    }

    @Test
    public void shouldNotFindAccounts() {
        when(accountDao.findAll()).thenReturn(new ArrayList<>());
        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                accountService.findAll()).withMessage("No accounts found");
    }

}
