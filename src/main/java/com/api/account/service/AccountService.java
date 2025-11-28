package com.api.account.service;

import com.api.account.model.Account;

import java.util.List;

public interface AccountService {

    List<Account> findAll();
    Account findById(Long id);
    Account save(Account account);
    void delete(Long id);
}
