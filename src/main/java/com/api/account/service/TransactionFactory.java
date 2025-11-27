package com.api.account.service;

import com.api.account.enumeration.TransactionType;
import com.api.account.exception.BusinessException;
import com.api.account.service.impl.DepositServiceImpl;
import com.api.account.service.impl.TransferServiceImpl;
import com.api.account.service.impl.WithdrawServiceImpl;

import java.util.Map;

import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;

public final class TransactionFactory {

    private TransactionFactory() {
    }

    private static final Map<TransactionType, TransactionService> SERVICES = Map.of(
            TransactionType.DEPOSIT, new DepositServiceImpl(),
            TransactionType.WITHDRAW, new WithdrawServiceImpl(),
            TransactionType.TRANSFER, new TransferServiceImpl()
    );

    public static TransactionService getService(TransactionType transactionType) {
        var service = SERVICES.get(transactionType);
        if (service == null) {
            throw new BusinessException(
                    HTTP_BAD_REQUEST_STATUS,
                    "Unsupported transaction type: " + transactionType
            );
        }
        return service;
    }

}
