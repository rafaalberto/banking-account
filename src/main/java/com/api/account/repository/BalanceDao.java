package com.api.account.repository;

import com.api.account.database.TransactionContext;
import com.api.account.model.Account;

public interface BalanceDao {

    Account updateBalance(Account account, TransactionContext transactionContext);

    void updateBalancesForTransfer(Account accountSender, Account accountReceiver);

}
