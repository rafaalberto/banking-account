package com.api.account;

import com.api.account.config.RoutesApplication;
import com.api.account.database.DatabaseConnection;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.api.account.utils.HttpUtils.APP_HOST;
import static com.api.account.utils.HttpUtils.APP_PORT;

public class AccountApiApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountApiApplication.class);

    public static void main(String[] args) {
        Undertow.Builder builder = Undertow.builder();
        builder.addHttpListener(APP_PORT, APP_HOST);
        builder.setHandler(RoutesApplication.ROUTES);

        Undertow server = builder.build();
        server.start();

        LOGGER.info("Application started at " + APP_PORT);

        DatabaseConnection.connect();
    }

}