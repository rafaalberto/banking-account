package com.api.account.service;

import com.api.account.enumeration.TransactionType;
import com.api.account.service.impl.DepositServiceImpl;
import com.api.account.service.impl.TransferServiceImpl;
import com.api.account.service.impl.WithdrawServiceImpl;
import java.util.Map;

public final class TransactionFactory {

  private final AccountService accountService;
  private final BalanceService balanceService;
  private final TransactionManager transactionManager;
  private final Map<TransactionType, TransactionService> services;

  public TransactionFactory(
      final AccountService accountService,
      final BalanceService balanceService,
      final TransactionManager transactionManager) {
    this.accountService = accountService;
    this.balanceService = balanceService;
    this.transactionManager = transactionManager;
    this.services = createServices();
  }

  private Map<TransactionType, TransactionService> createServices() {
    return Map.of(
        TransactionType.DEPOSIT,
            new DepositServiceImpl(accountService, balanceService, transactionManager),
        TransactionType.WITHDRAW,
            new WithdrawServiceImpl(accountService, balanceService, transactionManager),
        TransactionType.TRANSFER,
            new TransferServiceImpl(accountService, balanceService, transactionManager));
  }

  public TransactionService getService(final TransactionType transactionType) {
    return services.get(transactionType);
  }
}
