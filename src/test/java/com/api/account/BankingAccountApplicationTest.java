package com.api.account;

import com.api.account.repository.impl.AccountDaoImplTest;
import com.api.account.resource.AccountResourceTest;
import com.api.account.resource.TransactionResourceTest;
import com.api.account.service.impl.AccountServiceImplTest;
import com.api.account.service.impl.TransactionServiceImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static org.junit.runners.Suite.*;

@RunWith(Suite.class)
@SuiteClasses({
        AccountDaoImplTest.class,
        AccountServiceImplTest.class,
        TransactionServiceImplTest.class,
        AccountResourceTest.class,
        TransactionResourceTest.class
})

public class BankingAccountApplicationTest {

}
