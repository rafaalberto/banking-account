package com.api.account.service;

import com.api.account.exception.BusinessException;

import java.math.BigDecimal;

import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

public final class CalculationService {

    public static BigDecimal deposit(BigDecimal balance, BigDecimal amount) {
        return balance.add(amount);
    }

    public static BigDecimal withdraw(BigDecimal balance, BigDecimal amount) {
        if(balance.compareTo(amount) < BigDecimal.ZERO.intValue()) {
            throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Insufficient funds");
        }
        return balance.subtract(amount);
    }

}
