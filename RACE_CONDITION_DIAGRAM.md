# Race Condition Visual Explanation

## 🔴 The Problem: Lost Updates in Concurrent Transactions

This document visually explains how race conditions occur in the banking account system.

---

## 🌍 Real-World Analogy: Threads = Bank Tellers

**Threads are like bank tellers or cashiers processing transactions simultaneously:**

- **1 Thread** = 1 teller at the bank counter
- **10 Threads** = 10 tellers at 10 different counters, all working at the same time
- **Concurrent operations** = Multiple customers depositing/withdrawing from the same account at different counters simultaneously
- **Race condition** = When two tellers don't know what the other is doing and overwrite each other's work

### 🏦 Simple Example:
Imagine a bank with 10 teller windows. Customer "Alice" has $1000 in her account. Suddenly:
- **10 different people** (or the same person at different windows) try to deposit $500 each to Alice's account
- Each teller thinks: "Current balance is $1000, so after my deposit it will be $1500"
- But they all read $1000 at the same time, and their final updates overwrite each other!
- **Result:** Instead of ending with $6000, Alice only gets $1500 ❌

---

## 📊 Scenario: 2 Threads Depositing to the Same Account

**Think of it as: 2 Bank Tellers (Threads) processing deposits for the same account**

**Initial State:** Account #1 (Alice's account) has balance = **$1000**

**Real-World Scenario:** 
- Alice's friend Bob goes to **Teller Window 1** to deposit $500 to Alice's account
- Alice's friend Carol goes to **Teller Window 2** to deposit $500 to Alice's account
- Both happen at almost the same time (concurrent requests)

**Technical Equivalent:**
- Thread 1 = Teller Window 1 processing Bob's deposit
- Thread 2 = Teller Window 2 processing Carol's deposit

### ❌ Current Implementation (WITH Race Condition)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        TIME PROGRESSION                             │
└─────────────────────────────────────────────────────────────────────┘

T1: Teller 1 (Bob's deposit)                 T2: Teller 2 (Carol's deposit)
    deposit(accountId=1, amount=500)              deposit(accountId=1, amount=500)
    │                                                  │
    ├─> Check account balance (looks up in system)   │
    │   📋 Sees: Balance = $1000                      │
    │                                                 │
    │                                          ┌─────┼─> Check account balance (looks up in system)
    │                                          │     │   📋 Sees: Balance = $1000  ❌ SAME VALUE!
    │                                          │     │   (Before Teller 1 finishes!)
    │                                          │     │
    ├─> verifyData()                           │     ├─> verifyData()
    │                                          │     │
    ├─> 🔒 Locks account (only one teller at a time) ─┼─────┼─> [⏸️ WAITING - Account is locked]
    │                                          │     │
    │   💰 Calculate: 1000 + 500 = $1500      │     │
    │                                          │     │
    │   ✅ Update balance to $1500            │     │
    │   ✅ Save to system                      │     │
    │                                          │     │
    │   🔓 Unlock account ─────────────────────┼─────┼─> ✅ Teller 2 can now access account
    │                                          │     │
    │                                          │     ├─> 🔒 Locks account
    │                                          │     │
    │                                          │     │   📋 Uses OLD value she saw: $1000 ❌
    │                                          │     │   (Doesn't know Teller 1 already updated to $1500!)
    │                                          │     │   💰 Calculate: 1000 + 500 = $1500  ❌ WRONG!
    │                                          │     │
    │                                          │     │   ✅ Update balance to $1500
    │                                          │     │   ❌ OVERWRITES Teller 1's work!
    │                                          │     │   ✅ Save to system
    │                                          │     │
    │                                          │     │   🔓 Unlock account
    │                                          │     │
```

### 📉 Result Analysis

**Real-World Impact:**
- **Bob's deposit** ($500) was processed and saved ✅
- **Carol's deposit** ($500) overwrote Bob's work ❌
- Alice's account should have **$2000** but only has **$1500**
- **Lost money:** $500 (Bob's deposit was overwritten!)

```
Expected Final Balance: $1000 + $500 + $500 = $2000 ✅
Actual Final Balance:   $1500 ❌
Lost: $500 (Teller 1's/Bob's deposit was overwritten by Teller 2!)
```

---

## 🔍 Detailed Sequence Diagram (Bank Teller Analogy)

**Story:** Bob wants to deposit $500 to Alice's account. Carol also wants to deposit $500 to Alice's account. They go to different teller windows at the same time.

```
┌──────────┐        ┌──────────┐        ┌──────────────┐        ┌─────────────┐
│ Teller 1 │        │ Teller 2 │        │ Bank System  │        │ Code Logic  │
│  (Bob)   │        │ (Carol)  │        │  (Database)  │        │             │
└────┬─────┘        └────┬─────┘        └──────┬───────┘        └──────┬──────┘
     │                   │                      │                       │
     │ 1. findById(1)    │                      │                       │
     │──────────────────>│                      │                       │
     │                   │                      │                       │
     │                   │ 2. findById(1)       │                       │
     │                   │─────────────────────>│                       │
     │                   │                      │                       │
     │                   │ 3. RETURN {id:1,     │                       │
     │                   │    balance:1000}     │                       │
     │                   │<─────────────────────│                       │
     │                   │  ❌ Read $1000       │                       │
     │                   │                      │                       │
     │ 4. RETURN {id:1,  │                      │                       │
     │    balance:1000}  │                      │                       │
     │<──────────────────│                      │                       │
     │  ❌ Read $1000    │                      │                       │
     │                   │                      │                       │
     │                   │                      │                       │
     │ 5. synchronized   │                      │                       │
     │    (this) {       │                      │                       │
     │                   │                      │                       │
     │ 6. Calculate:     │                      │                       │
     │    1000 + 500     │                      │                       │
     │    = 1500         │                      │                       │
     │                   │                      │                       │
     │ 7. UPDATE         │                      │                       │
     │    balance=1500   │                      │                       │
     │─────────────────────────────────────────>│                       │
     │                   │                      │                       │
     │ 8. } Release      │                      │                       │
     │    lock           │                      │                       │
     │                   │                      │                       │
     │                   │ 9. synchronized      │                       │
     │                   │    (this) {          │                       │
     │                   │                      │                       │
     │                   │ 10. Calculate:       │                       │
     │                   │     ❌ Uses OLD      │                       │
     │                   │     value: 1000      │                       │
     │                   │     1000 + 500       │                       │
     │                   │     = 1500 ❌        │                       │
     │                   │                      │                       │
     │                   │ 11. UPDATE           │                       │
     │                   │     balance=1500     │                       │
     │                   │     (OVERWRITES!)    │                       │
     │                   │─────────────────────>│                       │
     │                   │                      │                       │
     │                   │ 12. } Release        │                       │
     │                   │     lock             │                       │
     │                   │                      │                       │
     │                   │                      │                       │
     │                   │ Final Balance: $1500 ❌                      │
     │                   │ (Should be $2000!)   │                       │
```

---

## 🎯 The Root Cause

### Code Flow in DepositServiceImpl.java:

```java
public void execute(Transaction transaction) {
    // ❌ PROBLEM 1: READ happens OUTSIDE synchronized block
    Account account = accountService.findById(...);  // Line 24
                                                      // Thread 1 reads: $1000
                                                      // Thread 2 reads: $1000 (BEFORE Thread 1 updates)
    
    verifyData(transaction);
    
    // ✅ Solution attempt: synchronized block
    synchronized (this) {                            // Line 28
        // ❌ PROBLEM 2: Uses stale data from line 24!
        account.setBalance(deposit(account.getBalance(), transaction.getAmount()));
        //                                 ^^^^^^^^^^^^^^
        //                                 Uses OLD value ($1000) even if another thread updated DB
        
        accountService.updateBalance(account);
        // This writes calculated balance, but it's based on OLD read!
    }
}
```

---

## 🔄 Visual: The Critical Section Problem

```
┌─────────────────────────────────────────────────────────────────────┐
│                  DEPOSIT EXECUTION FLOW                             │
└─────────────────────────────────────────────────────────────────────┘

Step 1: READ (UNPROTECTED - RACE CONDITION HERE! ❌)
┌─────────────────────────────────────────────────────┐
│ Account account = accountService.findById(id);      │
│ └─> Multiple threads can execute this simultaneously│
│ └─> They all read the SAME balance                  │
└─────────────────────────────────────────────────────┘
                        │
                        ▼
Step 2: VERIFY (UNPROTECTED)
┌─────────────────────────────────────────────────────┐
│ verifyData(transaction);                            │
└─────────────────────────────────────────────────────┘
                        │
                        ▼
Step 3: MODIFY & WRITE (PROTECTED - but too late! ❌)
┌─────────────────────────────────────────────────────┐
│ synchronized (this) {                               │
│     // Problem: Uses stale data from Step 1         │
│     account.setBalance(...);                        │
│     accountService.updateBalance(account);          │
│ }                                                   │
└─────────────────────────────────────────────────────┘

❌ Problem: Step 1 (read) happens OUTSIDE the critical section!
```

---

## 📈 Comparison: With vs Without Race Condition

### ❌ Current Implementation (WITH Race Condition) - The Broken Bank

**Story:** Multiple tellers processing deposits for the same account without proper coordination

```
Timeline:
│
├─ T0: Account Balance = $1000
│
├─ T1: Thread 1 reads $1000 ⏱️
├─ T2: Thread 2 reads $1000 ⏱️  ← Both read at same time!
│
├─ T3: Thread 1 calculates: 1000 + 500 = $1500
├─ T4: Thread 1 writes: balance = $1500 ✅
│
├─ T5: Thread 2 calculates: 1000 + 500 = $1500 ❌ (uses old value!)
├─ T6: Thread 2 writes: balance = $1500 ❌ (overwrites Thread 1)
│
└─ Final: $1500 (LOST: $500)
```

### ✅ Correct Implementation (WITHOUT Race Condition) - The Well-Managed Bank

**Story:** Only one teller can access an account at a time. Others must wait in line.

```
Timeline:
│
├─ T0: Account Balance = $1000
│
├─ T1: Thread 1 acquires lock 🔒
├─ T2: Thread 1 reads $1000
├─ T3: Thread 1 calculates: 1000 + 500 = $1500
├─ T4: Thread 1 writes: balance = $1500 ✅
├─ T5: Thread 1 releases lock 🔓
│
├─ T6: Thread 2 acquires lock 🔒 (waits for Thread 1)
├─ T7: Thread 2 reads $1500 ✅ (current value!)
├─ T8: Thread 2 calculates: 1500 + 500 = $2000 ✅
├─ T9: Thread 2 writes: balance = $2000 ✅
├─ T10: Thread 2 releases lock 🔓
│
└─ Final: $2000 ✅ (CORRECT!)
```

---

## 🔐 The Fix: Move READ Inside Synchronized Block

### ❌ Current (Broken):
```java
Account account = accountService.findById(...);  // ← OUTSIDE sync block ❌
synchronized (this) {
    account.setBalance(...);  // Uses stale data
    updateBalance(account);
}
```

### ✅ Fixed:
```java
synchronized (this) {
    Account account = accountService.findById(...);  // ← INSIDE sync block ✅
    account.setBalance(...);  // Uses fresh data
    updateBalance(account);
}
```

### ✅ Better Fix (Per-Account Locking):
```java
// Lock by account ID, not globally
synchronized (getAccountLock(accountId)) {
    Account account = accountService.findById(...);  // ← INSIDE sync block ✅
    account.setBalance(...);
    updateBalance(account);
}
```

---

## 🎬 Real-World Scenario: 10 Concurrent Deposits

```
Initial Balance: $1000

10 Threads each depositing $500
Expected Total: $1000 + ($500 × 10) = $6000

┌─────────────────────────────────────────────────────────────────┐
│ Thread 1: Reads $1000, Calculates $1500, Writes $1500          │
│ Thread 2: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 3: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 4: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 5: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 6: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 7: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 8: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 9: Reads $1000, Calculates $1500, Writes $1500 ❌        │
│ Thread 10: Reads $1000, Calculates $1500, Writes $1500 ❌       │
└─────────────────────────────────────────────────────────────────┘

Final Balance: $1500 (or random value depending on execution order)
Lost: $4500 ❌

Each thread reads the ORIGINAL balance ($1000) before any updates!
```

---

## 🔍 Database-Level Race Condition

Even with synchronized blocks, there's ANOTHER race condition at the database level:

```
Thread 1                                    Thread 2
────────────────────────────────────────    ────────────────────────────
SELECT balance FROM accounts               SELECT balance FROM accounts
WHERE id = 1                               WHERE id = 1
→ Returns: $1000                           → Returns: $1000
                                           │
[Calculate: 1000 + 500 = 1500]            [Calculate: 1000 + 500 = 1500]
                                           │
UPDATE accounts                            UPDATE accounts
SET balance = 1500                         SET balance = 1500
WHERE id = 1                               WHERE id = 1
```

**Solution:** Use `SELECT ... FOR UPDATE` to lock rows:
```sql
SELECT balance FROM accounts
WHERE id = 1
FOR UPDATE;  -- ← Locks the row until transaction completes
```

---

## 📝 Summary

### The Race Condition Chain:

1. **READ** happens outside synchronized block → Multiple threads read same value ❌
2. **CALCULATE** uses stale data → Wrong calculations ❌
3. **WRITE** overwrites other threads' updates → Lost updates ❌
4. **Final balance** is incorrect → Money disappears! ❌

### The Fix Chain:

1. **Move READ inside synchronized block** → Only one thread reads at a time ✅
2. **Use fresh data for calculation** → Correct calculations ✅
3. **Sequential writes** → No overwrites ✅
4. **Add database-level locking** → Prevents cross-instance race conditions ✅

---

## 🧪 How to See It in Action

Run the test:
```bash
./gradlew test --tests "ConcurrentTransactionRaceConditionTest"
```

You'll see output like:
```
Expected final balance: $10000
Actual final balance: $6500
Balance difference: $3500  ← RACE CONDITION PROVEN!
```

The difference proves that operations were lost due to race conditions.

