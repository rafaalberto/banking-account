package com.api.account.config;

import static io.undertow.util.Methods.DELETE;
import static io.undertow.util.Methods.GET;
import static io.undertow.util.Methods.POST;
import static io.undertow.util.Methods.PUT;

import com.api.account.resource.AccountResource;
import com.api.account.resource.TransactionResource;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;

public final class RoutesApplication {

  public static RoutingHandler createRoutes(
      final AccountResource accountResource, final TransactionResource transactionResource) {

    return new RoutingHandler()
        .add(GET, "/", RoutesApplication::index)
        .add(GET, "/accounts", accountResource::findAll)
        .add(GET, "/accounts/{id}", accountResource::findById)
        .add(POST, "/accounts", accountResource::create)
        .add(PUT, "/accounts/{id}", accountResource::update)
        .add(DELETE, "/accounts/{id}", accountResource::delete)
        .add(POST, "/transactions", transactionResource::execute);
  }

  private static void index(final HttpServerExchange exchange) {
    exchange.getResponseSender().send("Application Started");
  }
}
