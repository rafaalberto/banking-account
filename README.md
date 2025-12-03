# Banking Account API — Refactored in 2025

This project was originally developed in **2019** as part of a coding challenge for **Revolut**.  
In **2025**, the application is undergoing a **complete refactor** to improve concurrency safety, transactional integrity, architecture, and test coverage.

This repository demonstrates:
- A lightweight REST API in Java with no heavy frameworks
- Correct handling of financial transactions *(WIP)*
- Safe concurrent operations *(WIP)*
- Clean architecture and SOLID principles *(WIP)*
- Comprehensive unit, integration, and concurrency testing *(WIP)*

---

## 🚀 Technologies

- **Java 21** (updated Nov/2025)
- **Undertow** (embedded HTTP server)
- **JDBC + H2 Database**
- **REST Assured** (API testing)
- **JUnit 5**
- **Gradle**

---

## 🔄 Refactor 2025 — Improvements

### 🔴 Issues in the original 2019 version
- Race conditions during deposits, withdrawals, and transfers 
- Ineffective locking using `synchronized(this)`
- A single shared JDBC Connection instance (not thread-safe)
- Missing `rollback()` in multi-step database operations
- Violations of **DIP** (Dependency Inversion Principle)
- DAO mixing unrelated responsibilities
- No concurrency or integration tests

### 🟢 Fixes Implemented in 2025 *(WIP)*
- Migration to **Java 21** & build system migration to **Gradle** ✅
- Codebase cleanup and preparation for dependency injection ✅
- Improved folder structure and project organization ✅
- Cleanup of DAO responsibilities
- Documentation updates (README, ARCHITECTURE, templates)

### 📌 Upcoming improvements (Planned)
- Pessimistic row locking using `SELECT FOR UPDATE` ✅
- Replace single connection with connection pool (DataSource) ✅
- Introduce proper transactional rollback ✅
- Add concurrency tests (ExecutorService) ✅
- Add integration tests ✅
- Add exception hierarchy & structured logging

---

## ▶ Running the Application

Build the project:

```bash
./gradlew clean build
```

Start the server:

```bash
java -jar build/libs/banking-account.jar
```

The API will be available at:

```
http://localhost:8080
```

---

## 🧪 Running Tests

Unit tests:

```bash
./gradlew test
```

(Integration and concurrency tests coming as part of the ongoing refactor.)

---

# 📐 Architecture Overview

```
REST Controller → Service Layer → DAO → H2 Database
```

- The service layer contains business rules.
- The DAO layer manages SQL queries and transactions.
- Undertow handles HTTP routing without heavy frameworks.

More details are available in: **ARCHITECTURE.md**

---

# 📚 API Documentation

Below is the structure of the REST API as implemented in the original challenge.  
The refactor maintains backward compatibility while improving internal logic.

---

## 🔵 Accounts

### **Create Account**
**POST** `/accounts`

**Request**
```json
{
  "name": "Rafael"
}
```

**Response**
```json
{
  "id": 1,
  "name": "Rafael",
  "balance": 0.00
}
```

---

### **Update Account**
**PUT** `/accounts/1`

**Request**
```json
{
  "name": "John"
}
```

**Response**
```json
{
  "id": 1,
  "name": "John",
  "balance": 0.00
}
```

---

### **Delete Account**
**DELETE** `/accounts/1`

**Response**  
`204 No Content`

---

### **Get Account by ID**
**GET** `/accounts/1`

**Response**
```json
{
  "id": 1,
  "name": "Rafael",
  "balance": 0.00
}
```

---

### **List All Accounts**
**GET** `/accounts`

**Response**
```json
[
  {
    "id": 1,
    "name": "Rafael",
    "balance": 1000.00
  },
  {
    "id": 2,
    "name": "Mary",
    "balance": 2000.30
  },
  {
    "id": 3,
    "name": "Pedro",
    "balance": 800.00
  }
]
```

---

## 🟣 Transactions

All transaction types share the same endpoint.  
Behavior depends on the **type** field.

---

### **Deposit**
**POST** `/transactions`

**Request**
```json
{
  "accountSenderId": 1,
  "accountReceiverId": 1,
  "amount": 5000,
  "type": "DEPOSIT"
}
```

**Response**
```json
{
  "success": true,
  "description": "Deposit executed successfully"
}
```

---

### **Withdraw**
**POST** `/transactions`

**Request**
```json
{
  "accountSenderId": 1,
  "accountReceiverId": 1,
  "amount": 100,
  "type": "WITHDRAW"
}
```

**Response**
```json
{
  "success": true,
  "description": "Withdraw executed successfully"
}
```

---

### **Transfer**
**POST** `/transactions`

**Request**
```json
{
  "accountSenderId": 1,
  "accountReceiverId": 2,
  "amount": 500,
  "type": "TRANSFER"
}
```

**Response**
```json
{
  "success": true,
  "description": "Transfer executed successfully"
}
```

---

## 📌 Next Steps (WIP Roadmap)
- Add concurrency-safe operations
- Implement database-level locking
- Add DataSource & connection pooling
- Expand test coverage (integration + concurrency)
- Improve error handling and validation
- Containerize the application (Docker)

---

## ⭐ Final Notes
This repository is both an **educational refactor** and an **engineering case study** demonstrating improvements over time.  
More updates will be added as the refactor continues.