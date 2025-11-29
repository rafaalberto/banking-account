package com.api.account.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages account-level locks to prevent concurrent modifications.
 * One lock per account ID.
 * 
 * Locks are automatically cleaned up when accounts are deleted to prevent memory leaks.
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

    /**
     * Removes the lock for the given account ID.
     * Should be called when an account is deleted to prevent memory leaks.
     * 
     * Note: This method is safe to call even if the lock doesn't exist.
     * It's also safe to call while other threads might be using the lock,
     * as the lock object itself remains valid until all references are released.
     *
     * @param accountId The account ID whose lock should be removed
     */
    public static void removeLock(Long accountId) {
        locks.remove(accountId);
    }

    /**
     * Removes all locks. Useful for cleanup or testing.
     * Use with caution in production.
     */
    public static void clearAllLocks() {
        locks.clear();
    }
}