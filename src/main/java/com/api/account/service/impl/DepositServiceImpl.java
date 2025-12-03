package com.api.account.service.impl;

import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.service.AccountService;
import com.api.account.service.BalanceService;
import com.api.account.service.TransactionManager;
import com.api.account.service.TransactionService;

import static com.api.account.service.AccountLockManager.getLock;

import java.math.BigDecimal;

import static com.api.account.service.CalculationService.deposit;
import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

public class DepositServiceImpl implements TransactionService {

    private final AccountService accountService;
    private final BalanceService balanceService;
    private final TransactionManager transactionManager;

    public DepositServiceImpl(AccountService accountService, BalanceService balanceService, TransactionManager transactionManager) {
        this.accountService = accountService;
        this.balanceService = balanceService;
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Transaction transaction) {
        Long accountId = transaction.getAccountSenderId();

        synchronized (getLock(accountId)) {
            transactionManager.executeInTransaction(transactionContext -> {
                Account account = accountService.findByIdWithLock(accountId, transactionContext);
                verifyData(transaction);
                account.setBalance(deposit(account.getBalance(), transaction.getAmount()));
                balanceService.updateBalance(account, transactionContext);
                return null;
            });
        }
    }

    private void verifyData(Transaction transaction) {
        if (!transaction.getAccountSenderId().equals(transaction.getAccountReceiverId())) {
            throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Account Sender and Receiver must be the same");
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Amount must be greater than zero");
        }
    }
}
