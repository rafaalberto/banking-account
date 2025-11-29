package com.api.account.service.impl;

import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.service.AccountService;
import com.api.account.service.BalanceService;
import com.api.account.service.TransactionManager;
import com.api.account.service.TransactionService;
import com.api.account.utils.HttpUtils;

import java.math.BigDecimal;

import static com.api.account.service.AccountLockManager.getLock;
import static com.api.account.service.CalculationService.deposit;
import static com.api.account.service.CalculationService.withdraw;
import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

public class TransferServiceImpl implements TransactionService {

    private final AccountService accountService;
    private final BalanceService balanceService;
    private final TransactionManager transactionManager;


    public TransferServiceImpl(AccountService accountService, BalanceService balanceService, TransactionManager transactionManager) {
        this.accountService = accountService;
        this.balanceService = balanceService;
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Transaction transaction) {
        Long senderId = transaction.getAccountSenderId();
        Long receiverId = transaction.getAccountReceiverId();

        // Prevent deadlock: always lock accounts in same order (by ID)
        Object lock1 = getLock(Math.min(senderId, receiverId));
        Object lock2 = getLock(Math.max(senderId, receiverId));

        // Nested locks prevent deadlocks
        synchronized (lock1) {
            synchronized (lock2) {
                transactionManager.executeInTransaction(transactionContext -> {
                    Account accountSender = accountService.findByIdWithLock(senderId, transactionContext);
                    Account accountReceiver = accountService.findByIdWithLock(receiverId, transactionContext);

                    verifyData(transaction);

                    accountSender.setBalance(withdraw(accountSender.getBalance(), transaction.getAmount()));
                    accountReceiver.setBalance(deposit(accountReceiver.getBalance(), transaction.getAmount()));

                    balanceService.updateBalancesForTransfer(accountSender, accountReceiver, transactionContext);
                    return null;
                });
            }
        }
    }

    private void verifyData(Transaction transaction) {
        if(transaction.getAccountSenderId().equals(transaction.getAccountReceiverId())) {
            throw new BusinessException(HttpUtils.HTTP_BAD_REQUEST_STATUS, "Account Sender and Receiver must be different");
        }
        if(transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Amount must be greater than zero");
        }
    }
}
