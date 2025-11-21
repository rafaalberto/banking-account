package com.api.account.service.impl;

import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.service.AccountService;
import com.api.account.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static com.api.account.enumeration.TransactionType.*;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TransactionServiceImplTest {

    private Account account;

    @InjectMocks
    private TransactionService depositService = new DepositServiceImpl();

    @InjectMocks
    private TransactionService withdrawService = new WithdrawServiceImpl();

    @InjectMocks
    private TransactionService transferService = new TransferServiceImpl();

    @Mock
    private AccountService accountService = new AccountServiceImpl();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        account = new Account(1L, "Rafael");
    }

    @Test
    public void shouldDepositSuccessfully() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), DEPOSIT);
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);
        depositService.execute(transaction);
    }

    @Test
    public void shouldDenyDepositWithDifferentAccounts() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(1000)), DEPOSIT);
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                depositService.execute(transaction)).withMessage("Account Sender and Receiver must be the same");
    }

    @Test
    public void shouldDenyDepositWithAmountZero() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(BigDecimal.ZERO), DEPOSIT);
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                depositService.execute(transaction)).withMessage("Amount must be greater than zero");
    }

    @Test
    public void shouldWithdrawSuccessfully() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);
        withdrawService.execute(transaction);
    }

    @Test
    public void shouldDenyWithdrawWithDifferentAccounts() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                withdrawService.execute(transaction)).withMessage("Account Sender and Receiver must be the same");
    }

    @Test
    public void shouldDenyWithdrawWithAmountZero() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(BigDecimal.ZERO), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                withdrawService.execute(transaction)).withMessage("Amount must be greater than zero");
    }

    @Test
    public void shouldDenyWithdrawWithInsufficientFunds() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(500)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                withdrawService.execute(transaction)).withMessage("Insufficient funds");
    }

    @Test
    public void shouldTransferSuccessfully() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(1000)), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountReceiverId())).thenReturn(accountReceiver);

        transferService.execute(transaction);
    }

    @Test
    public void shouldDenyTransferWithSameAccount() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountReceiverId())).thenReturn(accountReceiver);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                transferService.execute(transaction)).withMessage("Account Sender and Receiver must be different");
    }

    @Test
    public void shouldDenyTransferWithAmountZero() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(BigDecimal.ZERO), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", new BigDecimal(1000));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", new BigDecimal(1000));
        Mockito.when(accountService.findById(transaction.getAccountReceiverId())).thenReturn(accountReceiver);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                transferService.execute(transaction)).withMessage("Amount must be greater than zero");
    }

    @Test
    public void shouldDenyTransferWithInsufficientFunds() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(convertTwoDecimalPlace(new BigDecimal(2000))), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", convertTwoDecimalPlace(new BigDecimal(500)));
        Mockito.when(accountService.findById(transaction.getAccountSenderId())).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findById(transaction.getAccountReceiverId())).thenReturn(accountReceiver);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                transferService.execute(transaction)).withMessage("Insufficient funds");
    }
}
