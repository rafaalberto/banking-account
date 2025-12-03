package com.api.account.unit.service.impl;

import com.api.account.database.TransactionContext;
import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.service.AccountService;
import com.api.account.service.BalanceService;
import com.api.account.service.TransactionManager;
import com.api.account.service.TransactionService;
import com.api.account.service.impl.DepositServiceImpl;
import com.api.account.service.impl.TransferServiceImpl;
import com.api.account.service.impl.WithdrawServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.api.account.enumeration.TransactionType.*;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    private Account account;

    @Mock
    private AccountService accountService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private TransactionContext transactionContext;

    private TransactionService depositService;

    private TransactionService withdrawService;

    private TransactionService transferService;

    @BeforeEach
    public void setUp() {
        account = new Account(1L, "Rafael");
        depositService = new DepositServiceImpl(accountService, balanceService, transactionManager);
        withdrawService = new WithdrawServiceImpl(accountService, balanceService, transactionManager);
        transferService = new TransferServiceImpl(accountService, balanceService, transactionManager);
        
        // Mock TransactionManager to execute the lambda
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            com.api.account.service.TransactionOperation<?> operation = invocation.getArgument(0);
            return operation.execute(transactionContext);
        });
    }

    @Test
    public void shouldDepositSuccessfully() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), DEPOSIT);
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);
        depositService.execute(transaction);
    }

    @Test
    public void shouldDenyDepositWithDifferentAccounts() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(1000)), DEPOSIT);
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                depositService.execute(transaction)).withMessage("Account Sender and Receiver must be the same");
    }

    @Test
    public void shouldDenyDepositWithAmountZero() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(BigDecimal.ZERO), DEPOSIT);
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                depositService.execute(transaction)).withMessage("Amount must be greater than zero");
    }

    @Test
    public void shouldWithdrawSuccessfully() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);
        withdrawService.execute(transaction);
    }

    @Test
    public void shouldDenyWithdrawWithDifferentAccounts() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                withdrawService.execute(transaction)).withMessage("Account Sender and Receiver must be the same");
    }

    @Test
    public void shouldDenyWithdrawWithAmountZero() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(BigDecimal.ZERO), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                withdrawService.execute(transaction)).withMessage("Amount must be greater than zero");
    }

    @Test
    public void shouldDenyWithdrawWithInsufficientFunds() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(500)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(account);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                withdrawService.execute(transaction)).withMessage("Insufficient funds");
    }

    @Test
    public void shouldTransferSuccessfully() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(1000)), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountReceiverId(), transactionContext)).thenReturn(accountReceiver);

        // Stub the balance update to prevent actual database call
        Mockito.doNothing().when(balanceService).updateBalancesForTransfer(
                Mockito.any(Account.class), 
                Mockito.any(Account.class), 
                Mockito.any(TransactionContext.class));

        transferService.execute(transaction);
    }

    @Test
    public void shouldDenyTransferWithSameAccount() {
        Transaction transaction = new Transaction(1L, 1L, convertTwoDecimalPlace(new BigDecimal(1000)), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountReceiverId(), transactionContext)).thenReturn(accountReceiver);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                transferService.execute(transaction)).withMessage("Account Sender and Receiver must be different");
    }

    @Test
    public void shouldDenyTransferWithAmountZero() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(BigDecimal.ZERO), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", new BigDecimal(1000));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", new BigDecimal(1000));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountReceiverId(), transactionContext)).thenReturn(accountReceiver);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                transferService.execute(transaction)).withMessage("Amount must be greater than zero");
    }

    @Test
    public void shouldDenyTransferWithInsufficientFunds() {
        Transaction transaction = new Transaction(1L, 2L, convertTwoDecimalPlace(new BigDecimal(2000)), TRANSFER);

        Account accountSender = new Account(1L, "Rafael", convertTwoDecimalPlace(new BigDecimal(500)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountSenderId(), transactionContext)).thenReturn(accountSender);

        Account accountReceiver = new Account(2L, "Mary", convertTwoDecimalPlace(new BigDecimal(1000)));
        Mockito.when(accountService.findByIdWithLock(transaction.getAccountReceiverId(), transactionContext)).thenReturn(accountReceiver);

        assertThatExceptionOfType(BusinessException.class).isThrownBy(() ->
                transferService.execute(transaction)).withMessage("Insufficient funds");
    }
}
