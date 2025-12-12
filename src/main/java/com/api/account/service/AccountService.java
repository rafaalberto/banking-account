package com.api.account.service;

import com.api.account.database.TransactionContext;
import com.api.account.model.Account;
import java.util.List;

public interface AccountService {

  List<Account> findAll();

  Account findById(Long id);

  Account findByIdWithLock(Long id, TransactionContext transactionContext);

  Account save(Account account);

  void delete(Long id);
}
