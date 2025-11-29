package com.api.account.service.impl;

import com.api.account.database.TransactionContext;
import com.api.account.model.Account;
import com.api.account.repository.BalanceDao;
import com.api.account.service.BalanceService;

public class BalanceServiceImpl implements BalanceService {

    private final BalanceDao balanceDao;

    public BalanceServiceImpl(BalanceDao balanceDao) {
        this.balanceDao = balanceDao;
    }

    @Override
    public void updateBalance(Account account, TransactionContext transactionContext) {
        balanceDao.updateBalance(account, transactionContext);
    }

    @Override
    public void updateBalancesForTransaction(Account accountSender, Account accountReceiver) {
        balanceDao.updateBalancesForTransfer(accountSender, accountReceiver);
    }

}
