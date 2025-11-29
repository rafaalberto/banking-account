package com.api.account.integration.concurrency;

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

class TransactionConcurrencyIntegrationTest {

    private AccountDao accountDao;
    private BalanceDao balanceDao;

    private AccountService accountService;
    private BalanceService balanceService;
    private TransactionManager transactionManager;
    private TransactionFactory transactionFactory;
    private TransactionContext transactionContext;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseConnection.startup();

        accountDao = new AccountDaoImpl();
        balanceDao = new BalanceDaoImpl();

        accountService = new AccountServiceImpl(accountDao);
        balanceService = new BalanceServiceImpl(balanceDao);
        transactionManager = new TransactionManagerImpl();
        transactionFactory = new TransactionFactory(accountService, balanceService, transactionManager);

        Connection connection = ConnectionFactory.getConnection();
        transactionContext = new TransactionContextImpl(connection);
    }

    @Test
    void shouldHandleConcurrentDeposits() throws InterruptedException {
        Account account = accountDao.insert(new Account("Mary"));
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        balanceDao.updateBalance(account, transactionContext);

        Long accountId = account.getId();
        BigDecimal depositAmount = convertTwoDecimalPlace(new BigDecimal(100));
        int numberOfThreads = 10;

        BigDecimal initialBalance = convertTwoDecimalPlace(new BigDecimal(1000));
        BigDecimal expectedFinalBalance = initialBalance.add(
                depositAmount.multiply(new BigDecimal(numberOfThreads))
        );

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // CountDownLatch(1) means: wait for 1 signal to start
        CountDownLatch startLatch = new CountDownLatch(1);

        // CountDownLatch(numberOfThreads) means: wait for all threads to finish
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);

        // AtomicInteger: thread-safe counter to track successes
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int index = 0; index < numberOfThreads; index++) {
            executor.submit(() -> {
                try {
                    // Wait for start signal (all threads wait here)
                    startLatch.await();

                    Transaction transaction = new Transaction(accountId, accountId, depositAmount, TransactionType.DEPOSIT);
                    transactionFactory.getService(TransactionType.DEPOSIT).execute(transaction);

                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // This releases all waiting threads at the same time
        startLatch.countDown();

        // Wait up to 30 seconds for all threads to finish
        boolean allCompleted = completionLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(allCompleted).as("All threads should complete within timeout").isTrue();
        assertThat(successCount.get()).as("All deposit operations should succeed").isEqualTo(numberOfThreads);
        assertThat(failureCount.get()).as("No operations should fail").isZero();

        Account finalAccount = accountDao.findById(accountId);
        BigDecimal actualFinalBalance = finalAccount.getBalance();

        assertThat(actualFinalBalance)
                .as("Final balance should be 2000.00")
                .isEqualByComparingTo(expectedFinalBalance);
    }

    @Test
    void shouldHandleConcurrentWithdrawals() throws InterruptedException {
        Account account = accountDao.insert(new Account("Mary"));
        account.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        balanceDao.updateBalance(account, transactionContext);

        Long accountId = account.getId();
        BigDecimal withdrawAmount = convertTwoDecimalPlace(new BigDecimal(50));
        int numberOfThreads = 10;

        BigDecimal expectedFinalBalance = new BigDecimal(500);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int index = 0; index < numberOfThreads; index++) {
            executor.submit(() -> {
                try {
                    // Wait for start signal (all threads wait here)
                    startLatch.await();

                    Transaction transaction = new Transaction(accountId, accountId, withdrawAmount, TransactionType.DEPOSIT);
                    transactionFactory.getService(TransactionType.WITHDRAW).execute(transaction);

                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failureCount.incrementAndGet();
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

        assertThat(allCompleted).as("All threads should complete within timeout").isTrue();
        assertThat(successCount.get()).as("All deposit operations should succeed").isEqualTo(numberOfThreads);
        assertThat(failureCount.get()).as("No operations should fail").isZero();

        Account finalAccount = accountDao.findById(accountId);
        BigDecimal actualFinalBalance = finalAccount.getBalance();

        assertThat(actualFinalBalance)
                .as("Final balance should be 500.00")
                .isEqualByComparingTo(expectedFinalBalance);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (transactionContext instanceof TransactionContextImpl) {
            ((TransactionContextImpl) transactionContext).getConnection().close();
        }
        accountDao.deleteAll();
    }

}
