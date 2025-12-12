package com.api.account.model;

import com.api.account.enumeration.TransactionType;
import java.math.BigDecimal;
import java.util.Objects;

public class Transaction {

  private Long accountSenderId;
  private Long accountReceiverId;
  private BigDecimal amount;
  private TransactionType type;

  public Transaction() {}

  public Transaction(
      final Long accountSenderId,
      final Long accountReceiverId,
      final BigDecimal amount,
      final TransactionType type) {
    this.accountSenderId = accountSenderId;
    this.accountReceiverId = accountReceiverId;
    this.amount = amount;
    this.type = type;
  }

  public Long getAccountSenderId() {
    return accountSenderId;
  }

  public Long getAccountReceiverId() {
    return accountReceiverId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public TransactionType getType() {
    return type;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Transaction that = (Transaction) o;
    return Objects.equals(accountSenderId, that.accountSenderId)
        && Objects.equals(accountReceiverId, that.accountReceiverId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountSenderId, accountReceiverId);
  }
}
