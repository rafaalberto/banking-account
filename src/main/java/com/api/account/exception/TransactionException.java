package com.api.account.exception;

public class TransactionException extends RuntimeException {

  public TransactionException(final String message) {
    super(message);
  }

  public TransactionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
