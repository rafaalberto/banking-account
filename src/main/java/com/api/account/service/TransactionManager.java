package com.api.account.service;

public interface TransactionManager {

    <T> T executeInTransaction(TransactionOperation<T> operation);
}
