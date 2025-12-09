package com.api.account.model;

import java.math.BigDecimal;

import static com.api.account.utils.NumericConverter.convertTwoDecimalPlace;

public record Account(Long id, String name, BigDecimal balance) {

    public Account {
        balance = convertTwoDecimalPlace(balance != null ? balance : BigDecimal.ZERO);
    }

    public Account(String name) {
        this(null, name, BigDecimal.ZERO);
    }

    public Account(Long id, String name) {
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

    public Account withId(Long newId) {
        return new Account(newId, this.name, this.balance);
    }

    public Account withName(String newName) {
        return new Account(this.id, newName, this.balance);
    }

    public Account withBalance(BigDecimal newBalance) {
        return new Account(this.id, this.name, newBalance);
    }

}