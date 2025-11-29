# Race Condition Testing Guide

## Overview

This guide provides step-by-step instructions for creating comprehensive race condition tests for the banking account application. These tests verify that concurrent operations on accounts maintain data integrity and don't lose any transactions.

---

## Table of Contents

1. [Understanding What You're Testing](#understanding-what-youre-testing)
2. [Test File Structure](#test-file-structure)
3. [Complete Example Test](#complete-example-test)
4. [Key Concepts Explained](#key-concepts-explained)
5. [Best Practices](#best-practices)
6. [Common Mistakes to Avoid](#common-mistakes-to-avoid)
7. [Implementation Checklist](#implementation-checklist)
8. [Running the Tests](#running-the-tests)
9. [What Success Looks Like](#what-success-looks-like)

---

## Understanding What You're Testing

**Goal:** Verify that multiple threads can operate on the same account simultaneously without losing data or creating inconsistencies.

**Example Scenario:**
- Account starts with $1000
- 10 threads each deposit $100
- Expected final balance: $2000
- **If there's a race condition:** Final balance might be less than $2000 (lost deposits)

---

## Test File Structure

Create this file:
```
src/test/java/com/api/account/concurrency/TransactionConcurrencyTest.java
```

---

## Complete Example Test

Here's a complete, detailed example you can use as a template:

```java
package com.api.account.concurrency;

import com.api.account.database.ConnectionFactory;
import com.api.account.database.DatabaseConnection;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.enumeration.TransactionType;
import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.repository.AccountDao;
import com.api.account.repository.BalanceDao;
import com.api.account.repository.impl.AccountDaoImpl;
import com.api.account.repository.impl.BalanceDaoImpl;
import com.api.account.service.AccountService;
import com.api.account.service.BalanceService;
import com.api.account.service.TransactionFactory;
import com.api.account.service.TransactionManager;
import com.api.account.service.impl.AccountServiceImpl;
import com.api.account.service.impl.BalanceServiceImpl;
import com.api.account.service.impl.TransactionManagerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for race conditions and thread safety in transaction operations.
 * 
 * These tests verify that concurrent operations on accounts maintain
 * data integrity and don't lose any transactions.
 */
public class TransactionConcurrencyTest {

    // ============================================
    // SETUP: Services and DAOs (like your other integration tests)
    // ============================================
    
    private AccountDao accountDao;
    private BalanceDao balanceDao;
    private AccountService accountService;
    private BalanceService balanceService;
    private TransactionManager transactionManager;
    private TransactionFactory transactionFactory;
    private TransactionContext transactionContext;

    @BeforeEach
    void setUp() throws SQLException {
        // Initialize database (same as your other integration tests)
        DatabaseConnection.startup();
        
        // Create DAOs
        accountDao = new AccountDaoImpl();
        balanceDao = new BalanceDaoImpl();
        
        // Create services (REAL services, not mocks - important for concurrency tests!)
        accountService = new AccountServiceImpl(accountDao);
        balanceService = new BalanceServiceImpl(balanceDao);
        transactionManager = new TransactionManagerImpl();
        transactionFactory = new TransactionFactory(accountService, balanceService, transactionManager);
        
        // Setup transaction context for helper methods
        Connection connection = ConnectionFactory.getConnection();
        transactionContext = new TransactionContextImpl(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up (same as your other integration tests)
        if (transactionContext instanceof TransactionContextImpl) {
            ((TransactionContextImpl) transactionContext).getConnection().close();
        }
        accountDao.deleteAll();
    }

    // ============================================
    // TEST 1: Concurrent Deposits (Simplest to understand)
    // ============================================
    
    /**
     * Test: 10 threads deposit $100 each to the same account simultaneously.
     * 
     * Scenario:
     * - Account starts with $1000
     * - 10 threads each deposit $100
     * - Expected final balance: $2000
     * 
     * If race condition exists: Some deposits might be lost, final balance < $2000
     */
    @Test
    void shouldHandleConcurrentDeposits() throws InterruptedException {
        // ===== STEP 1: Setup =====
        // Create account with initial balance
        Account account = accountDao.insert(new Account("TestAccount"));
        account.setBalance(convertTwoDecimalPlace(new BigDecimal("1000.00")));
        balanceDao.updateBalance(account, transactionContext);
        
        Long accountId = account.getId();
        BigDecimal depositAmount = convertTwoDecimalPlace(new BigDecimal("100.00"));
        int numberOfThreads = 10;
        
        // Expected final balance calculation
        BigDecimal initialBalance = convertTwoDecimalPlace(new BigDecimal("1000.00"));
        BigDecimal expectedFinalBalance = initialBalance.add(
            depositAmount.multiply(new BigDecimal(numberOfThreads))
        );
        
        // ===== STEP 2: Create thread pool =====
        // ExecutorService manages a pool of threads
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        // ===== STEP 3: Create synchronization tools =====
        // CountDownLatch(1) means: wait for 1 signal to start
        CountDownLatch startLatch = new CountDownLatch(1);
        
        // CountDownLatch(numberOfThreads) means: wait for all threads to finish
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        
        // AtomicInteger: thread-safe counter to track successes
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // ===== STEP 4: Submit tasks to thread pool =====
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    // Wait for start signal (all threads wait here)
                    startLatch.await();
                    
                    // Execute deposit operation
                    Transaction transaction = new Transaction(
                        accountId, 
                        accountId, 
                        depositAmount, 
                        TransactionType.DEPOSIT
                    );
                    
                    transactionFactory.getService(TransactionType.DEPOSIT)
                        .execute(transaction);
                    
                    // Operation succeeded
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    // Operation failed
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    // Signal that this thread is done
                    completionLatch.countDown();
                }
            });
        }
        
        // ===== STEP 5: Start all threads simultaneously =====
        // This releases all waiting threads at the same time
        startLatch.countDown();
        
        // ===== STEP 6: Wait for all threads to complete =====
        // Wait up to 30 seconds for all threads to finish
        boolean allCompleted = completionLatch.await(30, TimeUnit.SECONDS);
        
        // ===== STEP 7: Shutdown thread pool =====
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // ===== STEP 8: Verify results =====
        // Check that all threads completed
        assertThat(allCompleted)
            .as("All threads should complete within timeout")
            .isTrue();
        
        // Check that all operations succeeded
        assertThat(successCount.get())
            .as("All deposit operations should succeed")
            .isEqualTo(numberOfThreads);
        
        assertThat(failureCount.get())
            .as("No operations should fail")
            .isEqualTo(0);
        
        // Check final balance (THIS IS THE KEY ASSERTION!)
        Account finalAccount = accountDao.findById(accountId);
        BigDecimal actualFinalBalance = finalAccount.getBalance();
        
        assertThat(actualFinalBalance)
            .as("Final balance should equal initial + (deposit amount × number of threads)")
            .isEqualByComparingTo(expectedFinalBalance);
        
        // Additional verification: balance should be exactly $2000.00
        assertThat(actualFinalBalance)
            .as("Final balance should be $2000.00")
            .isEqualByComparingTo(convertTwoDecimalPlace(new BigDecimal("2000.00")));
    }

    // ============================================
    // TEST 2: Concurrent Withdrawals
    // ============================================
    
    @Test
    void shouldHandleConcurrentWithdrawals() throws InterruptedException {
        // Setup: Account with sufficient balance
        Account account = accountDao.insert(new Account("TestAccount"));
        account.setBalance(convertTwoDecimalPlace(new BigDecimal("1000.00")));
        balanceDao.updateBalance(account, transactionContext);
        
        Long accountId = account.getId();
        BigDecimal withdrawAmount = convertTwoDecimalPlace(new BigDecimal("50.00"));
        int numberOfThreads = 10; // 10 withdrawals of $50 = $500 total
        
        BigDecimal expectedFinalBalance = convertTwoDecimalPlace(new BigDecimal("500.00"));
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    Transaction transaction = new Transaction(
                        accountId, 
                        accountId, 
                        withdrawAmount, 
                        TransactionType.WITHDRAW
                    );
                    
                    transactionFactory.getService(TransactionType.WITHDRAW)
                        .execute(transaction);
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        boolean allCompleted = completionLatch.await(30, TimeUnit.SECONDS);
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Verify
        assertThat(allCompleted).isTrue();
        assertThat(successCount.get()).isEqualTo(numberOfThreads);
        
        Account finalAccount = accountDao.findById(accountId);
        assertThat(finalAccount.getBalance())
            .as("Final balance should be $500.00")
            .isEqualByComparingTo(expectedFinalBalance);
    }

    // ============================================
    // TEST 3: Concurrent Transfers (Most Complex)
    // ============================================
    
    @Test
    void shouldHandleConcurrentTransfers() throws InterruptedException {
        // Setup: Two accounts
        Account accountA = accountDao.insert(new Account("AccountA"));
        accountA.setBalance(convertTwoDecimalPlace(new BigDecimal("1000.00")));
        balanceDao.updateBalance(accountA, transactionContext);
        
        Account accountB = accountDao.insert(new Account("AccountB"));
        accountB.setBalance(convertTwoDecimalPlace(new BigDecimal("500.00")));
        balanceDao.updateBalance(accountB, transactionContext);
        
        Long accountAId = accountA.getId();
        Long accountBId = accountB.getId();
        BigDecimal transferAmount = convertTwoDecimalPlace(new BigDecimal("50.00"));
        int numberOfTransfers = 10; // 10 transfers of $50 each
        
        // Expected: A loses $500, B gains $500
        BigDecimal expectedBalanceA = convertTwoDecimalPlace(new BigDecimal("500.00"));
        BigDecimal expectedBalanceB = convertTwoDecimalPlace(new BigDecimal("1000.00"));
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfTransfers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfTransfers);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < numberOfTransfers; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    Transaction transaction = new Transaction(
                        accountAId, 
                        accountBId, 
                        transferAmount, 
                        TransactionType.TRANSFER
                    );
                    
                    transactionFactory.getService(TransactionType.TRANSFER)
                        .execute(transaction);
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        boolean allCompleted = completionLatch.await(30, TimeUnit.SECONDS);
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Verify
        assertThat(allCompleted).isTrue();
        assertThat(successCount.get()).isEqualTo(numberOfTransfers);
        
        Account finalAccountA = accountDao.findById(accountAId);
        Account finalAccountB = accountDao.findById(accountBId);
        
        assertThat(finalAccountA.getBalance())
            .as("Account A should have $500.00")
            .isEqualByComparingTo(expectedBalanceA);
        
        assertThat(finalAccountB.getBalance())
            .as("Account B should have $1000.00")
            .isEqualByComparingTo(expectedBalanceB);
        
        // Verify total money is preserved (no money created or lost)
        BigDecimal totalBefore = convertTwoDecimalPlace(new BigDecimal("1500.00"));
        BigDecimal totalAfter = finalAccountA.getBalance().add(finalAccountB.getBalance());
        assertThat(totalAfter)
            .as("Total money should be preserved")
            .isEqualByComparingTo(totalBefore);
    }

    // ============================================
    // TEST 4: Deadlock Prevention Test
    // ============================================
    
    /**
     * Test that deadlocks don't occur when transfers happen in opposite directions.
     * 
     * Thread 1: Transfer A → B
     * Thread 2: Transfer B → A
     * 
     * Without proper locking order: DEADLOCK!
     * With proper locking order: Both complete successfully
     */
    @Test
    void shouldPreventDeadlocks() throws InterruptedException {
        Account accountA = accountDao.insert(new Account("AccountA"));
        accountA.setBalance(convertTwoDecimalPlace(new BigDecimal("1000.00")));
        balanceDao.updateBalance(accountA, transactionContext);
        
        Account accountB = accountDao.insert(new Account("AccountB"));
        accountB.setBalance(convertTwoDecimalPlace(new BigDecimal("1000.00")));
        balanceDao.updateBalance(accountB, transactionContext);
        
        Long accountAId = accountA.getId();
        Long accountBId = accountB.getId();
        BigDecimal transferAmount = convertTwoDecimalPlace(new BigDecimal("100.00"));
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Thread 1: A → B
        executor.submit(() -> {
            try {
                startLatch.await();
                Transaction transaction = new Transaction(
                    accountAId, accountBId, transferAmount, TransactionType.TRANSFER
                );
                transactionFactory.getService(TransactionType.TRANSFER).execute(transaction);
                successCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                completionLatch.countDown();
            }
        });
        
        // Thread 2: B → A (opposite direction - potential deadlock!)
        executor.submit(() -> {
            try {
                startLatch.await();
                Transaction transaction = new Transaction(
                    accountBId, accountAId, transferAmount, TransactionType.TRANSFER
                );
                transactionFactory.getService(TransactionType.TRANSFER).execute(transaction);
                successCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                completionLatch.countDown();
            }
        });
        
        startLatch.countDown();
        
        // If deadlock occurs, this will timeout
        boolean allCompleted = completionLatch.await(10, TimeUnit.SECONDS);
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Verify no deadlock occurred
        assertThat(allCompleted)
            .as("Both transfers should complete without deadlock")
            .isTrue();
        
        assertThat(successCount.get())
            .as("Both transfers should succeed")
            .isEqualTo(2);
    }
}
```

---

## Key Concepts Explained

### 1. ExecutorService

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
```

**What it does:**
- Manages a pool of threads
- `newFixedThreadPool(10)` creates 10 threads
- Use `submit()` to run tasks

**Why use it:**
- Better than creating threads manually
- Reuses threads (more efficient)
- Easier to manage and shutdown

---

### 2. CountDownLatch

```java
CountDownLatch startLatch = new CountDownLatch(1);
CountDownLatch completionLatch = new CountDownLatch(10);
```

**What it does:**
- `startLatch`: Waits for 1 signal to start all threads
- `completionLatch`: Waits for 10 threads to finish
- `await()`: Waits until the count reaches 0
- `countDown()`: Decrements the count

**Why use it:**
- Synchronizes thread execution
- Ensures all threads start together (creates race condition)
- Ensures all threads finish before assertions

**How it works:**
1. Create latch with count (e.g., `new CountDownLatch(10)`)
2. Threads call `await()` - they wait here
3. Main thread calls `countDown()` - decrements count
4. When count reaches 0, all waiting threads are released

---

### 3. AtomicInteger

```java
AtomicInteger successCount = new AtomicInteger(0);
successCount.incrementAndGet();
```

**What it does:**
- Thread-safe counter
- Multiple threads can increment safely
- Use instead of `int` in concurrent code

**Why use it:**
- Regular `int` is not thread-safe
- `AtomicInteger` prevents race conditions in counting
- Essential for tracking successes/failures across threads

---

### 4. Why This Pattern Works

**The Flow:**

1. **All threads wait** on `startLatch.await()`
2. **Main thread signals** with `startLatch.countDown()` - releases all threads at once
3. **Threads execute concurrently** - this creates the race condition scenario
4. **Each thread signals completion** with `completionLatch.countDown()`
5. **Main thread waits** with `completionLatch.await()` - waits for all to finish
6. **Verify results** after all threads complete

**Visual Flow:**
```
Main Thread          Thread 1          Thread 2          Thread 3
     |                  |                 |                 |
     |--Create tasks-->  |                 |                 |
     |                  |                 |                 |
     |--Submit all----> |                 |                 |
     |                  |                 |                 |
     |                  |--await()------> |                 |
     |                  |                 |--await()------> |
     |                  |                 |                 |--await()
     |                  |                 |                 |
     |--countDown()----> |                 |                 |
     |                  |                 |                 |
     |                  |--Execute------> |                 |
     |                  |                 |--Execute------> |
     |                  |                 |                 |--Execute
     |                  |                 |                 |
     |                  |--countDown()---> |                 |
     |                  |                 |--countDown()---> |
     |                  |                 |                 |--countDown()
     |                  |                 |                 |
     |<--await()-------- |                 |                 |
     |                  |                 |                 |
     |--Verify results--|                 |                 |
```

---

## Best Practices

### 1. Start Simple

**Begin with 2-3 threads:**
- Easier to debug
- Faster to run
- Verify it works, then increase

**Then scale up:**
- 10 threads (good for most tests)
- 50-100 threads (stress testing)

---

### 2. Use Meaningful Assertions

```java
assertThat(actualBalance)
    .as("Final balance should be $2000.00")
    .isEqualByComparingTo(expectedBalance);
```

**Why:**
- The `as()` message helps debug failures
- Makes test output more readable
- Explains what you're testing

---

### 3. Always Wait for Completion

```java
boolean allCompleted = completionLatch.await(30, TimeUnit.SECONDS);
assertThat(allCompleted).isTrue();
```

**Why:**
- Prevents assertions from running before threads finish
- Detects deadlocks (timeout = deadlock)
- Ensures accurate results

---

### 4. Clean Up Resources

```java
executor.shutdown();
executor.awaitTermination(5, TimeUnit.SECONDS);
```

**Why:**
- Prevents resource leaks
- Ensures threads are properly terminated
- Good practice for all ExecutorService usage

---

### 5. Use Timeouts

```java
completionLatch.await(30, TimeUnit.SECONDS)
```

**Why:**
- Prevents tests from hanging forever
- Helps detect deadlocks
- Makes tests more reliable

**Choose appropriate timeout:**
- 10 seconds: Simple operations
- 30 seconds: Complex operations
- 60+ seconds: Stress tests

---

### 6. Track Successes and Failures

```java
AtomicInteger successCount = new AtomicInteger(0);
AtomicInteger failureCount = new AtomicInteger(0);
```

**Why:**
- Helps diagnose what went wrong
- Shows how many operations succeeded
- Useful for debugging

---

## Common Mistakes to Avoid

### Mistake 1: Not Synchronizing Start

**❌ WRONG - threads start at different times:**
```java
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        // Execute immediately - not synchronized!
        executeOperation();
    });
}
// Problem: Threads start at different times, no race condition!
```

**✅ CORRECT - all threads start together:**
```java
CountDownLatch startLatch = new CountDownLatch(1);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        startLatch.await(); // Wait for signal
        executeOperation(); // Execute
    });
}
startLatch.countDown(); // Release all at once
```

---

### Mistake 2: Asserting Before Threads Finish

**❌ WRONG - assertion runs before threads complete:**
```java
executor.submit(() -> { 
    executeOperation(); 
});
assertThat(balance).isEqualTo(expected); // Too early!
// Problem: Assertion might run before operation completes
```

**✅ CORRECT - wait for completion first:**
```java
CountDownLatch completionLatch = new CountDownLatch(10);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        executeOperation();
        completionLatch.countDown();
    });
}
completionLatch.await(); // Wait here
assertThat(balance).isEqualTo(expected); // Now safe
```

---

### Mistake 3: Using Regular int Instead of AtomicInteger

**❌ WRONG - not thread-safe:**
```java
int successCount = 0;
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        executeOperation();
        successCount++; // Race condition!
    });
}
// Problem: Multiple threads modifying same variable = data loss
```

**✅ CORRECT - thread-safe:**
```java
AtomicInteger successCount = new AtomicInteger(0);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        executeOperation();
        successCount.incrementAndGet(); // Thread-safe
    });
}
```

---

### Mistake 4: Not Using Real Services

**❌ WRONG - using mocks:**
```java
@Mock
private AccountService accountService;
// Problem: Mocks don't test real concurrency behavior
```

**✅ CORRECT - using real services:**
```java
AccountService accountService = new AccountServiceImpl(accountDao);
// Real services test actual thread safety
```

---

### Mistake 5: Not Cleaning Up Between Tests

**❌ WRONG - tests interfere with each other:**
```java
@Test
void test1() {
    // Uses account ID 1
}

@Test
void test2() {
    // Might use same account ID 1 - conflict!
}
```

**✅ CORRECT - clean up in @AfterEach:**
```java
@AfterEach
void tearDown() {
    accountDao.deleteAll(); // Clean up
}
```

---

## Implementation Checklist

Follow these steps in order:

- [ ] **Step 1:** Create the test file: `TransactionConcurrencyTest.java`
- [ ] **Step 2:** Copy the setup methods (`@BeforeEach`, `@AfterEach`)
- [ ] **Step 3:** Start with Test 1 (Concurrent Deposits) - simplest to understand
- [ ] **Step 4:** Run the test and verify it passes
- [ ] **Step 5:** Add Test 2 (Concurrent Withdrawals)
- [ ] **Step 6:** Add Test 3 (Concurrent Transfers)
- [ ] **Step 7:** Add Test 4 (Deadlock Prevention)
- [ ] **Step 8:** Run all tests and verify they pass
- [ ] **Step 9:** Run tests multiple times to catch intermittent issues
- [ ] **Step 10:** Increase thread count for stress testing (optional)

---

## Running the Tests

### Run all concurrency tests:
```bash
./gradlew test --tests "com.api.account.concurrency.*"
```

### Run a specific test:
```bash
./gradlew test --tests "com.api.account.concurrency.TransactionConcurrencyTest.shouldHandleConcurrentDeposits"
```

### Run with more output:
```bash
./gradlew test --tests "com.api.account.concurrency.*" --info
```

### Run integration tests (if you put them in integration package):
```bash
./gradlew integrationTest --tests "com.api.account.integration.concurrency.*"
```

---

## What Success Looks Like

When tests pass, you should see:

✅ **All threads complete successfully**
- No timeouts
- All operations finish

✅ **Final balances match expected values**
- No lost deposits
- No lost withdrawals
- Transfers are atomic

✅ **No exceptions thrown**
- All operations succeed
- No race condition errors

✅ **No deadlocks**
- All threads complete within timeout
- No hanging threads

✅ **All operations are accounted for**
- Success count matches thread count
- No lost operations

**Example output:**
```
TransactionConcurrencyTest > shouldHandleConcurrentDeposits() PASSED
TransactionConcurrencyTest > shouldHandleConcurrentWithdrawals() PASSED
TransactionConcurrencyTest > shouldHandleConcurrentTransfers() PASSED
TransactionConcurrencyTest > shouldPreventDeadlocks() PASSED

BUILD SUCCESSFUL
```

---

## Next Steps After Tests Pass

1. **Run tests multiple times** to catch intermittent issues
   ```bash
   ./gradlew test --tests "com.api.account.concurrency.*" --rerun-tasks
   ```

2. **Increase thread count** (10 → 50 → 100) to stress test
   - Modify `numberOfThreads` variable
   - Verify it still works under higher load

3. **Add more scenarios:**
   - Mixed operations (deposits + withdrawals + transfers)
   - Edge cases (zero balance, very large amounts)
   - Different account combinations

4. **Then proceed with Account immutability refactoring**
   - Tests will serve as safety net
   - Verify immutability doesn't break thread safety

---

## Additional Test Scenarios (Optional)

### Test 5: Mixed Operations
```java
@Test
void shouldHandleMixedConcurrentOperations() {
    // 5 deposits + 5 withdrawals + 5 transfers on same account
    // Verify final balance is correct
}
```

### Test 6: High Concurrency Stress Test
```java
@Test
void shouldHandleHighConcurrency() {
    // 100 threads operating on same account
    // Verify no data loss
}
```

### Test 7: Different Accounts (Should Be Parallel)
```java
@Test
void shouldHandleOperationsOnDifferentAccounts() {
    // 10 threads on Account A, 10 threads on Account B
    // Verify no interference
}
```

---

## Troubleshooting

### Test Fails: Balance is Wrong

**Possible causes:**
- Race condition exists (test is working - found a bug!)
- Test setup issue (check initial balance)
- Calculation error (check expected balance formula)

**Debug steps:**
1. Check initial balance
2. Check number of operations
3. Check final balance
4. Add logging to see what's happening

### Test Hangs (Timeout)

**Possible causes:**
- Deadlock occurred
- Thread pool not shutting down
- Database connection issue

**Debug steps:**
1. Check timeout value (increase if needed)
2. Check for deadlocks in code
3. Verify thread pool shutdown
4. Check database connections

### Test is Flaky (Sometimes Passes, Sometimes Fails)

**Possible causes:**
- Intermittent race condition
- Timing issue
- Resource contention

**Solutions:**
- Run test multiple times (`@RepeatedTest`)
- Increase thread count to make race conditions more likely
- Add more synchronization if needed

---

## Summary

This guide provides everything you need to create comprehensive race condition tests:

1. **Complete example code** - Copy and adapt
2. **Detailed explanations** - Understand each part
3. **Best practices** - Follow proven patterns
4. **Common mistakes** - Avoid pitfalls
5. **Step-by-step checklist** - Follow in order

**Remember:**
- Start simple (2-3 threads)
- Use real services (not mocks)
- Always wait for completion
- Verify final balances
- Clean up resources

Good luck with your testing! 🚀

