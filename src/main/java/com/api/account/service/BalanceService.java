package com.api.account.service;

import com.api.account.database.TransactionContext;
import com.api.account.model.Account;

public interface BalanceService {

  void updateBalance(Account account, TransactionContext transactionContext);

  void updateBalancesForTransfer(
      Account accountSender, Account accountReceiver, TransactionContext transactionContext);
}
