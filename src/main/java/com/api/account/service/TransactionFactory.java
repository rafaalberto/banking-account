package com.api.account.service;

import com.api.account.enumeration.TransactionType;
import com.api.account.exception.BusinessException;
import com.api.account.service.impl.DepositServiceImpl;
import com.api.account.service.impl.TransferServiceImpl;
import com.api.account.service.impl.WithdrawServiceImpl;

import java.util.Map;

import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

public final class TransactionFactory {

    private final AccountService accountService;
    private final BalanceService balanceService;
    private final Map<TransactionType, TransactionService> services;

    public TransactionFactory(AccountService accountService, BalanceService balanceService) {
        this.accountService = accountService;
        this.balanceService = balanceService;
        this.services = createServices();
    }

    private Map<TransactionType, TransactionService> createServices() {
        return Map.of(
                TransactionType.DEPOSIT, new DepositServiceImpl(accountService, balanceService),
                TransactionType.WITHDRAW, new WithdrawServiceImpl(accountService, balanceService),
                TransactionType.TRANSFER, new TransferServiceImpl(accountService, balanceService)
        );
    }

    public TransactionService getService(TransactionType transactionType) {
        return services.get(transactionType);
    }

}
