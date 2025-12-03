# Fix Race Conditions

## 🎯 Problem Statement

This PR addresses critical race condition and concurrency issues that could lead to:
- **Lost updates**: Concurrent transactions modifying the same account balance could overwrite each other
- **Inconsistent balances**: Race conditions between read and write operations
- **Deadlocks**: Potential deadlocks when transfers occur in opposite directions simultaneously
- **Data corruption**: Multiple threads modifying account balances without proper synchronization

### Example Race Condition Scenario

**Before the fix:**
```
Thread 1: Read balance ($1000) → Calculate new balance ($1100) → Write ($1100)
Thread 2: Read balance ($1000) → Calculate new balance ($1200) → Write ($1200)
Result: Lost update! Final balance is $1200 instead of $1300
```

## ✅ Solution Overview

This PR implements a **two-level locking strategy** to ensure thread-safety:

### 1. Application-Level Locking (`AccountLockManager`)
- **Per-account locks**: Each account has its own lock object stored in a `ConcurrentHashMap`
- **Synchronized blocks**: All balance-modifying operations use `synchronized` blocks
- **Parallel processing**: Different accounts can be modified concurrently (no global lock)
- **Memory management**: Locks are cleaned up when accounts are deleted

### 2. Database-Level Locking (`SELECT ... FOR UPDATE`)
- **Pessimistic locking**: Uses `SELECT ... FOR UPDATE` to lock rows at the database level
- **Transaction isolation**: Ensures ACID properties even if application locks are bypassed
- **Consistent reads**: Prevents dirty reads and ensures data consistency

### 3. Deadlock Prevention
- **Consistent lock ordering**: Transfers always lock accounts in the same order (by ID)
- **Algorithm**: `Math.min()` and `Math.max()` ensure consistent ordering regardless of transfer direction
- **Prevents circular waits**: Eliminates the possibility of deadlocks

## 🔧 Changes Made

### Core Implementation

#### 1. Created `AccountLockManager` (New)
```java
public class AccountLockManager {
    private static final Map<Long, Object> locks = new ConcurrentHashMap<>();
    
    public static Object getLock(Long accountId) {
        return locks.computeIfAbsent(accountId, k -> new Object());
    }
}
```

#### 2. Updated Transaction Services

**Deposit/Withdraw Services:**
- Wrapped operations in `synchronized(getLock(accountId))` blocks
- Added database-level locking with `findByIdWithLock()`

**Transfer Service:**
- Implemented consistent lock ordering to prevent deadlocks
- Locks accounts in order: `Math.min(senderId, receiverId)` then `Math.max(senderId, receiverId)`

#### 3. Enhanced DAO Layer
- Added `findByIdWithLock()` method using `SELECT ... FOR UPDATE`
- Maintains backward compatibility with existing `findById()` method

#### 4. Transaction Management
- All balance modifications now occur within database transactions
- Proper rollback on failures
- ACID compliance maintained

## 🧪 Testing

### Comprehensive Concurrency Tests Added

#### 1. `shouldHandleConcurrentDeposits()`
- Tests 10 concurrent deposits to the same account
- Verifies final balance is correct (no lost updates)
- Ensures all operations succeed

#### 2. `shouldHandleConcurrentWithdrawals()`
- Tests 10 concurrent withdrawals from the same account
- Verifies final balance is correct
- Validates insufficient funds detection

#### 3. `shouldHandleConcurrentTransfers()`
- Tests concurrent transfers between two accounts
- Verifies both account balances are correct
- Ensures total money is preserved (no money created or lost)

#### 4. `shouldPreventDeadlocks()` ⭐
- **Critical test**: Simulates deadlock scenario
- Thread 1: Transfer A → B
- Thread 2: Transfer B → A (opposite direction)
- Verifies both transfers complete successfully without deadlock
- Validates that consistent lock ordering prevents deadlocks

### Test Results
- ✅ All concurrency tests pass
- ✅ No race conditions detected
- ✅ No deadlocks observed
- ✅ Balance integrity maintained under high concurrency

## 📊 Impact

### Before
- ❌ Race conditions possible
- ❌ Lost updates could occur
- ❌ Potential deadlocks
- ❌ No concurrency tests

### After
- ✅ Thread-safe operations
- ✅ No lost updates
- ✅ Deadlock prevention
- ✅ Comprehensive concurrency tests
- ✅ ACID compliance
- ✅ Parallel processing for different accounts

## 🔍 Technical Details

### Lock Granularity
- **Fine-grained**: One lock per account (not a global lock)
- **Benefit**: Different accounts can be modified in parallel
- **Performance**: Better throughput for concurrent operations on different accounts

### Lock Ordering Algorithm
```java
// Always lock in same order to prevent deadlocks
Object lock1 = getLock(Math.min(senderId, receiverId));
Object lock2 = getLock(Math.max(senderId, receiverId));

synchronized (lock1) {
    synchronized (lock2) {
        // Transfer logic
    }
}
```

### Database Locking
```sql
SELECT * FROM accounts WHERE id = ? FOR UPDATE
```
- Locks the row until transaction commits
- Prevents other transactions from modifying the same row
- Works even if application-level locks fail

## 🚀 Performance Considerations

- **No performance degradation**: Locks are per-account, not global
- **Parallel processing**: Operations on different accounts proceed concurrently
- **Minimal overhead**: `ConcurrentHashMap` provides efficient lock storage
- **Database locks**: Released immediately after transaction commit

## 📝 Additional Improvements

- Added proper exception handling in transaction services
- Improved error messages for concurrent operation failures
- Enhanced logging for debugging concurrent operations
- Code cleanup and refactoring for better maintainability

## ✅ Verification

- [x] All existing tests pass
- [x] New concurrency tests pass
- [x] No race conditions detected
- [x] Deadlock prevention verified
- [x] Code review completed
- [x] Performance tested under load

## 🔗 Related Issues

Fixes race condition and concurrency issues identified in the original code review.

---

**Note**: This PR addresses critical thread-safety concerns and ensures the banking application can handle concurrent operations safely and correctly.


