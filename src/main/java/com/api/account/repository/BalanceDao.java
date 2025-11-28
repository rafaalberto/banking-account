package com.api.account.repository;

import com.api.account.model.Account;

public interface BalanceDao {

    Account updateBalance(Account account);

    void updateBalancesForTransfer(Account accountSender, Account accountReceiver);

}
