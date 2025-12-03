package com.api.account.service;

import com.api.account.database.TransactionContext;

@FunctionalInterface
public interface TransactionOperation<T> {
    T execute(TransactionContext transactionContext);
}
