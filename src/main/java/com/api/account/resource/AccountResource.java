package com.api.account.resource;

import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.service.AccountService;
import com.fasterxml.jackson.core.type.TypeReference;
import io.undertow.server.HttpServerExchange;

import java.util.List;

import static com.api.account.utils.HttpUtils.*;
import static com.api.account.utils.JsonConverter.convertToJson;
import static com.api.account.utils.JsonConverter.readFromJson;

public class AccountResource {

    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    public void create(HttpServerExchange exchange) {
        handleStatusAndHeaders(exchange, HTTP_CREATED_STATUS);
        exchange.getRequestReceiver().receiveFullString((serverExchange, message) -> {
            try {
                Account account = readFromJson(message, new TypeReference<>() {
                });
                if (account != null) {
                    account = accountService.save(account);
                    exchange.getResponseSender().send(convertToJson(account));
                }
            } catch (BusinessException e) {
                handleApplicationException(exchange, e);
            } catch (Exception e) {
                handleApplicationException(exchange, e);
            }
        });
    }

    public void update(HttpServerExchange exchange) {
        handleStatusAndHeaders(exchange, HTTP_OK_STATUS);
        String id = getQueryParameter(exchange);
        exchange.getRequestReceiver().receiveFullString((serverExchange, message) -> {
            try {
                Account account = readFromJson(message, new TypeReference<>() {
                });
                if (account != null) {
                    account = account.withId(Long.valueOf(id));
                    account = accountService.save(account);
                    exchange.getResponseSender().send(convertToJson(account));
                }
            } catch (BusinessException e) {
                handleApplicationException(exchange, e);
            } catch (Exception e) {
                handleApplicationException(exchange, e);
            }
        });
    }

    public void delete(HttpServerExchange exchange) {
        handleStatusAndHeaders(exchange, HTTP_NO_CONTENT_STATUS);
        try {
            String id = getQueryParameter(exchange);
            accountService.delete(Long.valueOf(id));
        } catch (BusinessException e) {
            handleApplicationException(exchange, e);
        } catch (Exception e) {
            handleApplicationException(exchange, e);
        }
    }

    public void findAll(HttpServerExchange exchange) {
        handleStatusAndHeaders(exchange, HTTP_OK_STATUS);
        try {
            List<Account> accounts = accountService.findAll();
            exchange.getResponseSender().send(convertToJson(accounts));
        } catch (BusinessException e) {
            handleApplicationException(exchange, e);
        } catch (Exception e) {
            handleApplicationException(exchange, e);
        }
    }

    public void findById(HttpServerExchange exchange) {
        handleStatusAndHeaders(exchange, HTTP_OK_STATUS);
        try {
            String id = getQueryParameter(exchange);
            Account account = accountService.findById(Long.valueOf(id));
            exchange.getResponseSender().send(convertToJson(account));
        } catch (BusinessException e) {
            handleApplicationException(exchange, e);
        } catch (Exception e) {
            handleApplicationException(exchange, e);
        }
    }

}
