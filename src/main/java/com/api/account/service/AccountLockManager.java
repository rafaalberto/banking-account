package com.api.account.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages account-level locks to prevent concurrent modifications.
 * One lock per account ID.
 */
public class AccountLockManager {

    private static final Map<Long, Object> locks = new ConcurrentHashMap<>();

    /**
     * Gets or creates a lock for the given account ID.
     * Threads modifying the same account will block each other.
     * Threads modifying different accounts can proceed in parallel.
     *
     * @param accountId The account ID
     * @return Lock object for the account
     */
    public static Object getLock(Long accountId) {
        return locks.computeIfAbsent(accountId, k -> new Object());
    }
}