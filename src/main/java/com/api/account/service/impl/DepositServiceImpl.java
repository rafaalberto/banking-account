package com.api.account.service.impl;

import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.service.AccountService;
import com.api.account.service.TransactionService;

import java.math.BigDecimal;

import static com.api.account.service.CalculationService.deposit;
import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

public class DepositServiceImpl implements TransactionService {

    private final AccountService accountService;

    public DepositServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void execute(Transaction transaction) {
        Account account = accountService.findById(transaction.getAccountSenderId());

        verifyData(transaction);

        account.setBalance(deposit(account.getBalance(), transaction.getAmount()));
        accountService.updateBalance(account);

    }

    private void verifyData(Transaction transaction) {
        if(!transaction.getAccountSenderId().equals(transaction.getAccountReceiverId())) {
            throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Account Sender and Receiver must be the same");
        }
        if(transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Amount must be greater than zero");
        }
    }
}
