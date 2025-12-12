package com.api.account;

import static com.api.account.utils.HttpUtils.APP_HOST;
import static com.api.account.utils.HttpUtils.APP_PORT;

import com.api.account.config.RoutesApplication;
import com.api.account.database.ConnectionFactory;
import com.api.account.database.DatabaseConnection;
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
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankingAccountApplication {

  private static final Logger LOGGER = LoggerFactory.getLogger(BankingAccountApplication.class);

  public static void main(final String[] args) {
    DatabaseConnection.startup();
    LOGGER.info("Database started");

    AccountDao accountDao = new AccountDaoImpl();
    BalanceDao balanceDao = new BalanceDaoImpl();
    AccountService accountService = new AccountServiceImpl(accountDao);
    BalanceService balanceService = new BalanceServiceImpl(balanceDao);
    TransactionManager transactionManager = new TransactionManagerImpl();
    TransactionFactory transactionFactory =
        new TransactionFactory(accountService, balanceService, transactionManager);

    AccountResource accountResource = new AccountResource(accountService);
    TransactionResource transactionResource = new TransactionResource(transactionFactory);

    RoutingHandler routes = RoutesApplication.createRoutes(accountResource, transactionResource);

    Undertow.Builder builder = Undertow.builder();
    builder.addHttpListener(APP_PORT, APP_HOST);
    builder.setHandler(routes);

    Undertow server = builder.build();
    server.start();

    LOGGER.info("Application started at " + APP_PORT);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  ConnectionFactory.shutdown();
                  LOGGER.info("Connection pool closed");
                }));
  }
}
