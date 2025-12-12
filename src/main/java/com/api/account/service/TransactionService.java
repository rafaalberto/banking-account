package com.api.account.service;

import com.api.account.model.Transaction;

public interface TransactionService {

  void execute(Transaction transaction);
}
