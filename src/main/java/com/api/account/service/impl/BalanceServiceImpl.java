package com.api.account.service.impl;

import com.api.account.database.TransactionContext;
import com.api.account.model.Account;
import com.api.account.repository.BalanceDao;
import com.api.account.service.BalanceService;

public class BalanceServiceImpl implements BalanceService {

  private final BalanceDao balanceDao;

  public BalanceServiceImpl(final BalanceDao balanceDao) {
    this.balanceDao = balanceDao;
  }

  @Override
  public void updateBalance(final Account account, final TransactionContext transactionContext) {
    balanceDao.updateBalance(account, transactionContext);
  }

  @Override
  public void updateBalancesForTransfer(
      final Account accountSender,
      final Account accountReceiver,
      final TransactionContext transactionContext) {
    balanceDao.updateBalancesForTransfer(accountSender, accountReceiver, transactionContext);
  }
}
