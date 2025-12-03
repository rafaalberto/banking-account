package com.api.account.integration.resource;

import com.api.account.config.RoutesApplication;
import com.api.account.database.ConnectionFactory;
import com.api.account.database.DatabaseConnection;
import com.api.account.database.TransactionContext;
import com.api.account.database.impl.TransactionContextImpl;
import com.api.account.model.Account;
import com.api.account.model.Message;
import com.api.account.model.Transaction;
import com.api.account.repository.AccountDao;
import com.api.account.repository.BalanceDao;
import com.api.account.unit.utils.TestDatabaseUtils;
import com.api.account.repository.impl.AccountDaoImpl;
import com.api.account.repository.impl.BalanceDaoImpl;
import com.api.account.resource.AccountResource;
import com.api.account.resource.TransactionResource;
import com.api.account.service.AccountService;
import com.api.account.service.BalanceService;
import com.api.account.service.TransactionFactory;
import com.api.account.service.TransactionManager;
import com.api.account.service.impl.AccountServiceImpl;
import com.api.account.service.impl.BalanceServiceImpl;
import com.api.account.service.impl.TransactionManagerImpl;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.undertow.Undertow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static com.api.account.enumeration.TransactionType.*;
import static com.api.account.utils.HttpUtils.*;
import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResourceIntegrationTest {

    private static final String RESOURCE_PATH = "/transactions";

    private AccountDao accountDao = new AccountDaoImpl();
    private BalanceDao balanceDao = new BalanceDaoImpl();

    private TransactionContext transactionContext;

    private Undertow server;

    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseConnection.startup();

        this.accountDao = new AccountDaoImpl();
        this.balanceDao = new BalanceDaoImpl();

        Connection connection = ConnectionFactory.getConnection();
        transactionContext = new TransactionContextImpl(connection);

        AccountService accountService = new AccountServiceImpl(accountDao);
        BalanceService balanceService = new BalanceServiceImpl(balanceDao);
        AccountResource accountResource = new AccountResource(accountService);
        TransactionManager transactionManager = new TransactionManagerImpl();
        TransactionFactory transactionFactory = new TransactionFactory(accountService, balanceService, transactionManager);
        TransactionResource transactionResource = new TransactionResource(transactionFactory);

        Undertow.Builder builder = Undertow.builder();
        builder.addHttpListener(TEST_PORT, APP_HOST);
        builder.setHandler(RoutesApplication.createRoutes(accountResource, transactionResource));
        server = builder.build();
        server.start();

        RestAssured.baseURI = "http://" + APP_HOST + ":" + TEST_PORT;
    }

    @Test
    public void shouldDepositSuccessfully() {
        Account accountInserted = insertAccount(new Account("Mary"));

        Transaction transaction = new Transaction(accountInserted.getId(), accountInserted.getId(), convertTwoDecimalPlace(new BigDecimal(1000)), DEPOSIT);

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(transaction)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_CREATED_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Deposit executed successfully");

        deleteAccount(accountInserted.getId());
    }

    @Test
    public void shouldDenyDepositWithAccountNotFound() {
        Account accountNotFound = new Account(1L, "Rafael");

        Transaction transaction = new Transaction(accountNotFound.getId(), accountNotFound.getId(), convertTwoDecimalPlace(new BigDecimal(1000)), DEPOSIT);

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(transaction)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_NOT_FOUND_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Account not found");
    }

    @Test
    public void shouldWithdrawSuccessfully() {
        Account accountInserted = insertAccount(new Account("Mary"));
        accountInserted.setBalance(convertTwoDecimalPlace(new BigDecimal(2000)));
        updateBalance(accountInserted, transactionContext);

        Transaction transaction = new Transaction(accountInserted.getId(), accountInserted.getId(), convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(transaction)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_CREATED_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Withdraw executed successfully");

        deleteAccount(accountInserted.getId());
    }

    @Test
    public void shouldDenyWithdrawWithInsufficientFunds() {
        Account accountInserted = insertAccount(new Account("Mary"));

        Transaction transaction = new Transaction(accountInserted.getId(), accountInserted.getId(), convertTwoDecimalPlace(new BigDecimal(1000)), WITHDRAW);

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(transaction)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_BAD_REQUEST_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Insufficient funds");

        deleteAccount(accountInserted.getId());
    }

    @Test
    public void shouldTransferSuccessfully() {
        Account accountSender = insertAccount(new Account("Mary"));
        accountSender.setBalance(convertTwoDecimalPlace(new BigDecimal(2000)));
        updateBalance(accountSender, transactionContext);

        Account accountReceiver = insertAccount(new Account("Rafael"));
        accountReceiver.setBalance(convertTwoDecimalPlace(new BigDecimal(1000)));
        updateBalance(accountReceiver, transactionContext);

        Transaction transaction = new Transaction(accountSender.getId(), accountReceiver.getId(), convertTwoDecimalPlace(new BigDecimal(1000)), TRANSFER);

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(transaction)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_CREATED_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Transfer executed successfully");

        deleteAccount(accountSender.getId());
        deleteAccount(accountReceiver.getId());
    }

    @Test
    public void shouldDenyTransferWithSameAccount() {
        Account accountSender = insertAccount(new Account("Mary"));
        accountSender.setBalance(convertTwoDecimalPlace(new BigDecimal(2000)));
        updateBalance(accountSender, transactionContext);

        Transaction transaction = new Transaction(accountSender.getId(), accountSender.getId(), convertTwoDecimalPlace(new BigDecimal(1000)), TRANSFER);

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(transaction)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_BAD_REQUEST_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Account Sender and Receiver must be different");

        deleteAccount(accountSender.getId());
    }

    @AfterEach
    public void finish() throws SQLException {
        if (server != null) {
            server.stop();
        }
        if (transactionContext instanceof TransactionContextImpl) {
            ((TransactionContextImpl) transactionContext).getConnection().close();
        }
        TestDatabaseUtils.deleteAllAccounts();
    }

    private Account insertAccount(Account account) {
        return accountDao.insert(account);
    }

    private void updateBalance(Account account, TransactionContext transactionContext) {
        balanceDao.updateBalance(account, transactionContext);
    }

    private void deleteAccount(Long accountId) {
        accountDao.delete(accountId);
    }
}
