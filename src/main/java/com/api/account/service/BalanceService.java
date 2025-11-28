package com.api.account.service;

import com.api.account.model.Account;

public interface BalanceService {

    void updateBalance(Account account);
    void updateBalancesForTransaction(Account accountSender, Account accountReceiver);
}
