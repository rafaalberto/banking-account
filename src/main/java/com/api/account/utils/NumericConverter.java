package com.api.account.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class NumericConverter {

    private static final int DECIMAL_PLACE = 2;

    public static BigDecimal convertTwoDecimalPlace(BigDecimal value) {
        return value.setScale(DECIMAL_PLACE, RoundingMode.FLOOR);
    }
}
