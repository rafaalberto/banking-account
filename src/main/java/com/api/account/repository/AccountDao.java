package com.api.account.repository;

import com.api.account.model.Account;

import java.util.List;

public interface AccountDao {

    Account insert(Account account);

    Account update(Account account);

    void delete(Long id);

    void deleteAll();

    List<Account> findAll();

    Account findById(Long id);

}
