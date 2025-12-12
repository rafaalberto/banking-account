package com.api.account.database.impl;

import com.api.account.database.TransactionContext;
import java.sql.Connection;

/** Internal implementation that wraps JDBC Connection. Not exposed to service layer. */
public class TransactionContextImpl implements TransactionContext {

  private final Connection connection;

  public TransactionContextImpl(final Connection connection) {
    this.connection = connection;
  }

  public Connection getConnection() {
    return connection;
  }
}
