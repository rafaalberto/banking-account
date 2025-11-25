# Banking Account API — Refactored in 2025

This project was originally developed in **2019** as part of a coding challenge for **Revolut**.  
In **2025**, the application is undergoing a **complete refactor** to improve concurrency safety, transactional integrity, architecture, and test coverage.

This repository demonstrates:
- A lightweight REST API in Java with no heavy frameworks
- Correct handling of financial transactions *(WIP)*
- Safe and consistent concurrent operations *(WIP)*
- Clean architecture and SOLID principles *(WIP)*
- Comprehensive unit, integration, and concurrency testing *(WIP)*

---

## 🚀 Technologies

- **Java 21** (updated in Nov/2025)
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
- A single shared Connection instance (not thread-safe)
- Missing `rollback()` in multi-step database operations
- Violations of **DIP** (Dependency Inversion Principle)
- DAO mixing unrelated responsibilities
- No concurrency tests or integration tests

### 🟢 Fixes Implemented in 2025 *(WIP)*
- Architecture cleanup and separation of responsibilities
- Migration to Java 21 & Gradle
- Codebase restructuring and preparation for dependency injection
- Improved project organization and documentation

Additional improvements (including concurrency fixes, atomic DB operations, and DataSource-based connections) are currently in progress.
