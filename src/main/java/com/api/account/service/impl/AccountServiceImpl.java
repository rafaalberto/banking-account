package com.api.account.service.impl;

import static com.api.account.service.AccountLockManager.removeLock;
import static com.api.account.utils.HttpUtils.HTTP_BAD_REQUEST_STATUS;
import static com.api.account.utils.HttpUtils.HTTP_NOT_FOUND_STATUS;
import static java.util.Optional.ofNullable;

import com.api.account.database.TransactionContext;
import com.api.account.exception.BusinessException;
import com.api.account.model.Account;
import com.api.account.repository.AccountDao;
import com.api.account.service.AccountService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class AccountServiceImpl implements AccountService {

  private final AccountDao accountDao;

  public AccountServiceImpl(final AccountDao accountDao) {
    this.accountDao = accountDao;
  }

  @Override
  public List<Account> findAll() {
    List<Account> accounts = accountDao.findAll();
    if (accounts.isEmpty()) {
      throw new BusinessException(HTTP_NOT_FOUND_STATUS, "No accounts found");
    }
    return accounts;
  }

  @Override
  public Account findById(final Long id) {
    Optional<Account> account = ofNullable(accountDao.findById(id));
    return account.orElseThrow(
        () -> new BusinessException(HTTP_NOT_FOUND_STATUS, "Account not found"));
  }

  @Override
  public Account findByIdWithLock(final Long id, final TransactionContext transactionContext) {
    Account account = accountDao.findByIdWithLock(id, transactionContext);
    if (account == null) {
      throw new BusinessException(HTTP_NOT_FOUND_STATUS, "Account not found");
    }
    return account;
  }

  @Override
  public Account save(final Account account) {
    verifyData(account);
    if (account.getId() == null) {
      verifyBalanceAmount(account);
      return accountDao.insert(account);
    } else {
      return accountDao.update(account);
    }
  }

  @Override
  public void delete(final Long id) {
    Account account = findById(id);
    accountDao.delete(account.getId());
    removeLock(id);
  }

  private void verifyData(final Account account) {
    if (StringUtils.isBlank(account.getName())) {
      throw new BusinessException(HTTP_BAD_REQUEST_STATUS, "Name must be informed");
    }
    if (account.getId() != null) {
      verifyIfExists(account.getId());
    }
  }

  private void verifyBalanceAmount(final Account account) {
    if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
      throw new BusinessException(
          HTTP_BAD_REQUEST_STATUS, "Balance is not allowed to be saved for this operation");
    }
  }

  private void verifyIfExists(final Long id) {
    Optional<Account> account = ofNullable(accountDao.findById(id));
    if (account.isPresent()) {
      return;
    }
    throw new BusinessException(HTTP_NOT_FOUND_STATUS, "Account not found");
  }
}
