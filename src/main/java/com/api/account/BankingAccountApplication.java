package com.api.account;

import com.api.account.config.RoutesApplication;
import com.api.account.database.ConnectionFactory;
import com.api.account.database.DatabaseConnection;
import com.api.account.repository.AccountDao;
import com.api.account.repository.impl.AccountDaoImpl;
import com.api.account.resource.AccountResource;
import com.api.account.resource.TransactionResource;
import com.api.account.service.AccountService;
import com.api.account.service.TransactionFactory;
import com.api.account.service.impl.AccountServiceImpl;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.api.account.utils.HttpUtils.APP_HOST;
import static com.api.account.utils.HttpUtils.APP_PORT;

public class BankingAccountApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankingAccountApplication.class);

    public static void main(String[] args) {
        DatabaseConnection.startup();
        LOGGER.info("Database started");

        AccountDao accountDao = new AccountDaoImpl();
        AccountService accountService = new AccountServiceImpl(accountDao);
        TransactionFactory transactionFactory = new TransactionFactory(accountService);

        AccountResource accountResource = new AccountResource(accountService);
        TransactionResource transactionResource = new TransactionResource(transactionFactory);

        RoutingHandler routes = RoutesApplication.createRoutes(accountResource, transactionResource);

        Undertow.Builder builder = Undertow.builder();
        builder.addHttpListener(APP_PORT, APP_HOST);
        builder.setHandler(routes);

        Undertow server = builder.build();
        server.start();

        LOGGER.info("Application started at " + APP_PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConnectionFactory.shutdown();
            LOGGER.info("Connection pool closed");
        }));
    }

}