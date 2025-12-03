package com.api.account.repository;

import com.api.account.database.TransactionContext;
import com.api.account.model.Account;

import java.util.List;

public interface AccountDao {

    Account insert(Account account);

    Account update(Account account);

    void delete(Long id);

    List<Account> findAll();

    Account findById(Long id);

    Account findByIdWithLock(Long id, TransactionContext context);

}
