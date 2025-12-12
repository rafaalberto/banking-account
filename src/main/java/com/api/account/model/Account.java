package com.api.account.model;

import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;

import java.math.BigDecimal;

public record Account(Long id, String name, BigDecimal balance) {

  public Account {
    balance = convertTwoDecimalPlace(balance != null ? balance : BigDecimal.ZERO);
  }

  public Account(final String name) {
    this(null, name, BigDecimal.ZERO);
  }

  public Account(final Long id, final String name) {
    this(id, name, BigDecimal.ZERO);
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public Account withId(final Long newId) {
    return new Account(newId, this.name, this.balance);
  }

  public Account withName(final String newName) {
    return new Account(this.id, newName, this.balance);
  }

  public Account withBalance(final BigDecimal newBalance) {
    return new Account(this.id, this.name, newBalance);
  }
}
