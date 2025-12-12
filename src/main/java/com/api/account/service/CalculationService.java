package com.api.account.service;

import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

import com.api.account.exception.BusinessException;
import java.math.BigDecimal;

public final class CalculationService {

  public static BigDecimal deposit(final BigDecimal balance, final BigDecimal amount) {
    return balance.add(amount);
  }

  public static BigDecimal withdraw(final BigDecimal balance, final BigDecimal amount) {
    if (balance.compareTo(amount) < BigDecimal.ZERO.intValue()) {
      throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Insufficient funds");
    }
    return balance.subtract(amount);
  }
}
