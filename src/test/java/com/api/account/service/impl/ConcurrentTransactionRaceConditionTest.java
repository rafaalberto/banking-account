package com.api.account.service.impl;

import com.api.account.model.Account;
import com.api.account.model.Transaction;
import com.api.account.repository.AccountDao;
import com.api.account.repository.impl.AccountDaoImpl;
import com.api.account.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.api.account.enumeration.TransactionType.*;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test demonstrates the race condition issues in the transaction service.
 * 
 * The race conditions occur because:
 * 1. Account balance is read OUTSIDE the synchronized block
 * 2. Multiple threads can read the same initial balance before any enters the synchronized block
 * 3. This causes lost updates where some transactions overwrite others
 * 
 * EXPECTED BEHAVIOR: These tests will likely FAIL or show incorrect balances,
 * proving that race conditions exist in the current implementation.
 */
public class ConcurrentTransactionRaceConditionTest {

    private AccountDao accountDao;
    private TransactionService depositService;
    private TransactionService withdrawService;
    private TransactionService transferService;
    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 10;

    @BeforeEach
    public void setUp() {
        accountDao = new AccountDaoImpl();
        depositService = new DepositServiceImpl();
        withdrawService = new WithdrawServiceImpl();
        transferService = new TransferServiceImpl();
    }

    @AfterEach
    public void tearDown() {
        accountDao.deleteAll();
    }

    /**
     * Test 1: Concurrent Deposits Race Condition
     * 
     * Scenario: 10 threads each deposit $100 to the same account 10 times
     * Expected result: Final balance should be $10,000 (100 * 10 * 10)
     * Actual result: Will be less due to lost updates from race conditions
     */
    @Test
    public void shouldDemonstrateRaceConditionWithConcurrentDeposits() throws InterruptedException {
        // Setup: Create account with initial balance of $0
        Account account = accountDao.insert(new Account("TestAccount"));
        Long accountId = account.getId();

        BigDecimal depositAmount = convertTwoDecimalPlace(new BigDecimal(100));
        BigDecimal expectedFinalBalance = convertTwoDecimalPlace(
            depositAmount.multiply(BigDecimal.valueOf(THREAD_COUNT))
                       .multiply(BigDecimal.valueOf(OPERATIONS_PER_THREAD))
        );

        // Create latch to coordinate all threads starting simultaneously
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Submit all deposit tasks
        for (int i = 0; i < THREAD_COUNT; i++) {
            Future<?> future = executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();

                    // Perform multiple deposits per thread
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            Transaction transaction = new Transaction(
                                accountId, accountId, depositAmount, DEPOSIT
                            );
                            depositService.execute(transaction);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            System.err.println("Deposit failed: " + e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
            futures.add(future);
        }

        // Start all threads simultaneously
        System.out.println("Starting " + THREAD_COUNT + " threads for concurrent deposits...");
        startLatch.countDown();

        // Wait for all threads to complete
        completionLatch.await();

        // Get final account balance
        Account finalAccount = accountDao.findById(accountId);
        BigDecimal actualFinalBalance = finalAccount.getBalance();

        System.out.println("===========================================");
        System.out.println("CONCURRENT DEPOSITS RACE CONDITION TEST");
        System.out.println("===========================================");
        System.out.println("Threads: " + THREAD_COUNT);
        System.out.println("Operations per thread: " + OPERATIONS_PER_THREAD);
        System.out.println("Total expected operations: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("Successful operations: " + successCount.get());
        System.out.println("Failed operations: " + failureCount.get());
        System.out.println("Expected final balance: $" + expectedFinalBalance);
        System.out.println("Actual final balance: $" + actualFinalBalance);
        System.out.println("Balance difference: $" + expectedFinalBalance.subtract(actualFinalBalance));
        System.out.println("===========================================");

        // This assertion will likely FAIL, demonstrating the race condition
        // The actual balance will be less than expected due to lost updates
        assertThat(actualFinalBalance)
            .as("Expected balance to be exactly $%s but was $%s. This proves race condition exists!",
                expectedFinalBalance, actualFinalBalance)
            .isEqualTo(expectedFinalBalance);

        executor.shutdown();
    }

    /**
     * Test 2: Concurrent Withdrawals Race Condition
     * 
     * Scenario: Account starts with $5,000. 10 threads each withdraw $50, 10 times
     * Expected result: Final balance should be $0 (5000 - (50 * 10 * 10))
     * Actual result: May show negative balance or incorrect balance due to race conditions
     */
    @Test
    public void shouldDemonstrateRaceConditionWithConcurrentWithdrawals() throws InterruptedException {
        // Setup: Create account with initial balance of $5,000
        Account account = accountDao.insert(new Account("TestAccount"));
        BigDecimal initialBalance = convertTwoDecimalPlace(new BigDecimal(5000));
        account.setBalance(initialBalance);
        accountDao.updateBalance(account);

        Long accountId = account.getId();
        BigDecimal withdrawAmount = convertTwoDecimalPlace(new BigDecimal(50));
        BigDecimal totalWithdrawn = convertTwoDecimalPlace(
            withdrawAmount.multiply(BigDecimal.valueOf(THREAD_COUNT))
                         .multiply(BigDecimal.valueOf(OPERATIONS_PER_THREAD))
        );
        BigDecimal expectedFinalBalance = initialBalance.subtract(totalWithdrawn);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            Transaction transaction = new Transaction(
                                accountId, accountId, withdrawAmount, WITHDRAW
                            );
                            withdrawService.execute(transaction);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        System.out.println("Starting concurrent withdrawals...");
        startLatch.countDown();
        completionLatch.await();

        Account finalAccount = accountDao.findById(accountId);
        BigDecimal actualFinalBalance = finalAccount.getBalance();

        System.out.println("===========================================");
        System.out.println("CONCURRENT WITHDRAWALS RACE CONDITION TEST");
        System.out.println("===========================================");
        System.out.println("Initial balance: $" + initialBalance);
        System.out.println("Total expected to withdraw: $" + totalWithdrawn);
        System.out.println("Expected final balance: $" + expectedFinalBalance);
        System.out.println("Actual final balance: $" + actualFinalBalance);
        System.out.println("Successful withdrawals: " + successCount.get());
        System.out.println("Failed withdrawals: " + failureCount.get());
        System.out.println("===========================================");

        // This will likely fail or show incorrect balance
        assertThat(actualFinalBalance)
            .as("Balance mismatch indicates race condition")
            .isEqualTo(expectedFinalBalance);

        executor.shutdown();
    }

    /**
     * Test 3: Concurrent Transfers Race Condition
     * 
     * Scenario: Account A starts with $10,000, Account B with $0
     * 10 threads each transfer $100 from A to B, 10 times
     * Expected result: A should have $0, B should have $10,000
     * Actual result: Incorrect balances due to race conditions
     */
    @Test
    public void shouldDemonstrateRaceConditionWithConcurrentTransfers() throws InterruptedException {
        // Setup: Create two accounts
        Account accountA = accountDao.insert(new Account("AccountA"));
        BigDecimal initialBalanceA = convertTwoDecimalPlace(new BigDecimal(10000));
        accountA.setBalance(initialBalanceA);
        accountDao.updateBalance(accountA);

        Account accountB = accountDao.insert(new Account("AccountB"));
        BigDecimal initialBalanceB = convertTwoDecimalPlace(BigDecimal.ZERO);
        accountB.setBalance(initialBalanceB);
        accountDao.updateBalance(accountB);

        Long accountAId = accountA.getId();
        Long accountBId = accountB.getId();

        BigDecimal transferAmount = convertTwoDecimalPlace(new BigDecimal(100));
        BigDecimal totalTransferred = convertTwoDecimalPlace(
            transferAmount.multiply(BigDecimal.valueOf(THREAD_COUNT))
                         .multiply(BigDecimal.valueOf(OPERATIONS_PER_THREAD))
        );
        BigDecimal expectedBalanceA = initialBalanceA.subtract(totalTransferred);
        BigDecimal expectedBalanceB = initialBalanceB.add(totalTransferred);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            Transaction transaction = new Transaction(
                                accountAId, accountBId, transferAmount, TRANSFER
                            );
                            transferService.execute(transaction);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        System.out.println("Starting concurrent transfers...");
        startLatch.countDown();
        completionLatch.await();

        Account finalAccountA = accountDao.findById(accountAId);
        Account finalAccountB = accountDao.findById(accountBId);
        BigDecimal actualBalanceA = finalAccountA.getBalance();
        BigDecimal actualBalanceB = finalAccountB.getBalance();

        BigDecimal totalBalance = actualBalanceA.add(actualBalanceB);
        BigDecimal expectedTotal = initialBalanceA.add(initialBalanceB);

        System.out.println("===========================================");
        System.out.println("CONCURRENT TRANSFERS RACE CONDITION TEST");
        System.out.println("===========================================");
        System.out.println("Account A - Initial: $" + initialBalanceA);
        System.out.println("Account A - Expected: $" + expectedBalanceA);
        System.out.println("Account A - Actual: $" + actualBalanceA);
        System.out.println("Account B - Initial: $" + initialBalanceB);
        System.out.println("Account B - Expected: $" + expectedBalanceB);
        System.out.println("Account B - Actual: $" + actualBalanceB);
        System.out.println("Total balance (A+B) - Expected: $" + expectedTotal);
        System.out.println("Total balance (A+B) - Actual: $" + totalBalance);
        System.out.println("Successful transfers: " + successCount.get());
        System.out.println("Failed transfers: " + failureCount.get());
        System.out.println("===========================================");

        // Check that total balance is preserved (money shouldn't disappear or appear)
        assertThat(totalBalance)
            .as("Total balance should be preserved. Money loss indicates race condition!")
            .isEqualTo(expectedTotal);

        // These will likely fail, showing race conditions
        assertThat(actualBalanceA)
            .as("Account A balance incorrect - race condition detected")
            .isEqualTo(expectedBalanceA);
        
        assertThat(actualBalanceB)
            .as("Account B balance incorrect - race condition detected")
            .isEqualTo(expectedBalanceB);

        executor.shutdown();
    }

    /**
     * Test 4: Mixed Concurrent Operations Race Condition
     * 
     * Scenario: Account starts with $5,000
     * - 5 threads depositing $100 each, 10 times
     * - 5 threads withdrawing $100 each, 10 times
     * Expected result: Final balance should still be $5,000 (balanced operations)
     * Actual result: Will show incorrect balance due to race conditions
     */
    @Test
    public void shouldDemonstrateRaceConditionWithMixedConcurrentOperations() throws InterruptedException {
        // Setup: Create account with initial balance of $5,000
        Account account = accountDao.insert(new Account("TestAccount"));
        BigDecimal initialBalance = convertTwoDecimalPlace(new BigDecimal(5000));
        account.setBalance(initialBalance);
        accountDao.updateBalance(account);

        Long accountId = account.getId();
        BigDecimal operationAmount = convertTwoDecimalPlace(new BigDecimal(100));
        int depositThreads = 5;
        int withdrawThreads = 5;

        // Expected: deposits and withdrawals should cancel out
        BigDecimal expectedFinalBalance = initialBalance;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(depositThreads + withdrawThreads);

        ExecutorService executor = Executors.newFixedThreadPool(depositThreads + withdrawThreads);
        AtomicInteger depositSuccess = new AtomicInteger(0);
        AtomicInteger withdrawSuccess = new AtomicInteger(0);

        // Submit deposit threads
        for (int i = 0; i < depositThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            Transaction transaction = new Transaction(
                                accountId, accountId, operationAmount, DEPOSIT
                            );
                            depositService.execute(transaction);
                            depositSuccess.incrementAndGet();
                        } catch (Exception e) {
                            // Ignore for this test
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Submit withdraw threads
        for (int i = 0; i < withdrawThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            Transaction transaction = new Transaction(
                                accountId, accountId, operationAmount, WITHDRAW
                            );
                            withdrawService.execute(transaction);
                            withdrawSuccess.incrementAndGet();
                        } catch (Exception e) {
                            // Ignore for this test
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        System.out.println("Starting mixed concurrent operations...");
        startLatch.countDown();
        completionLatch.await();

        Account finalAccount = accountDao.findById(accountId);
        BigDecimal actualFinalBalance = finalAccount.getBalance();

        System.out.println("===========================================");
        System.out.println("MIXED CONCURRENT OPERATIONS RACE CONDITION TEST");
        System.out.println("===========================================");
        System.out.println("Initial balance: $" + initialBalance);
        System.out.println("Expected final balance: $" + expectedFinalBalance);
        System.out.println("Actual final balance: $" + actualFinalBalance);
        System.out.println("Successful deposits: " + depositSuccess.get());
        System.out.println("Successful withdrawals: " + withdrawSuccess.get());
        System.out.println("Balance difference: $" + initialBalance.subtract(actualFinalBalance).abs());
        System.out.println("===========================================");

        // This will likely fail, showing race conditions in mixed operations
        assertThat(actualFinalBalance)
            .as("Balance should remain unchanged with balanced deposits/withdrawals. Difference indicates race condition!")
            .isEqualTo(expectedFinalBalance);

        executor.shutdown();
    }
}

