package com.api.account.enumeration;

public enum TransactionType {
  DEPOSIT("Deposit"),
  TRANSFER("Transfer"),
  WITHDRAW("Withdraw");

  private final String description;

  TransactionType(final String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
