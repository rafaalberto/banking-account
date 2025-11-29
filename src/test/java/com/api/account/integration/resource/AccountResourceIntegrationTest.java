package com.api.account.integration.resource;

import com.api.account.config.RoutesApplication;
import com.api.account.database.DatabaseConnection;
import com.api.account.model.Account;
import com.api.account.model.Message;
import com.api.account.repository.AccountDao;
import com.api.account.repository.BalanceDao;
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
import com.api.account.utils.NumericConverter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.undertow.Undertow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.api.account.utils.HttpUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountResourceIntegrationTest {

    private static final String RESOURCE_PATH = "/accounts";

    private Undertow server;

    private AccountDao accountDao;

    private BalanceDao balanceDao;

    @BeforeEach
    public void setUp() {
        DatabaseConnection.startup();

        this.accountDao = new AccountDaoImpl();
        this.balanceDao = new BalanceDaoImpl();
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
    public void shouldCreateAccountSuccessfully() {
        Account accountToInsert = new Account("Mary");
        Account accountInserted = insertAccount(accountToInsert);

        Account result = RestAssured.given().contentType(ContentType.JSON)
                .body(accountToInsert)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_CREATED_STATUS).extract().as(Account.class);

        assertThat(result.getName()).isEqualTo("Mary");
        assertThat(result.getBalance()).isEqualTo(NumericConverter.convertTwoDecimalPlace(BigDecimal.ZERO));

        deleteAccount(accountInserted.getId());
    }

    @Test
    public void shouldDenyToCreateAccount() {
        Account accountToInsert = new Account("");

        Message result = RestAssured.given().contentType(ContentType.JSON)
                .body(accountToInsert)
                .post(RESOURCE_PATH)
                .then().statusCode(HTTP_BAD_REQUEST_STATUS).extract().as(Message.class);

        assertThat(result.getDescription()).isEqualTo("Name must be informed");
    }

    @Test
    public void shouldDeleteAccountSuccessfully() {
        Account accountToInsert = new Account("Mary");
        Account accountInserted = insertAccount(accountToInsert);

        RestAssured.given().contentType(ContentType.JSON)
                .body(accountToInsert)
                .delete(RESOURCE_PATH + "/" + accountInserted.getId())
                .then().statusCode(HTTP_NO_CONTENT_STATUS);

        deleteAccount(accountInserted.getId());
    }

    @Test
    public void shouldFindByIdSuccessfully() {
        Account accountToInsert = new Account("Mary");
        Account accountInserted = insertAccount(accountToInsert);

        Account result = RestAssured.given().contentType(ContentType.JSON)
                .body(accountToInsert)
                .get(RESOURCE_PATH + "/" + accountInserted.getId())
                .then().statusCode(HTTP_OK_STATUS).extract().as(Account.class);

        assertThat(result.getName()).isEqualTo("Mary");
        assertThat(result.getBalance()).isEqualTo(NumericConverter.convertTwoDecimalPlace(BigDecimal.ZERO));

        deleteAccount(accountInserted.getId());
    }

    @Test
    public void shouldNotFindAccountById() {
        Account accountToFind = new Account(999L, "Rafael");
        RestAssured.given().contentType(ContentType.JSON)
                .body(accountToFind)
                .get(RESOURCE_PATH + "/" + accountToFind.getId())
                .then().statusCode(HTTP_NOT_FOUND_STATUS).extract().as(Message.class);
    }

    private Account insertAccount(Account account) {
        return accountDao.insert(account);
    }

    private void deleteAccount(Long accountId) {
        accountDao.delete(accountId);
    }

    @AfterEach
    public void finish() {
        server.stop();
        accountDao.deleteAll();
    }
}
