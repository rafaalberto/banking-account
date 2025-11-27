package com.api.account.enumeration;

public enum TransactionType {

    DEPOSIT("Deposit"),
    TRANSFER("Transfer"),
    WITHDRAW("Withdraw");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
