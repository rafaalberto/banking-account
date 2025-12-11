# 🏦 Banking Account API — Refactored in 2025

[![CI](https://img.shields.io/github/actions/workflow/status/rafaalberto/banking-account/ci.yml?label=CI&logo=githubactions&logoColor=white)](https://github.com/rafaalberto/banking-account/actions/workflows/ci.yml)
[![Quality Gate](https://img.shields.io/github/actions/workflow/status/rafaalberto/banking-account/ci.yml?label=Quality%20Gate&logo=checkmarx&logoColor=white)](https://github.com/rafaalberto/banking-account/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

A lightweight, framework-free banking API originally developed in **2019** as part of a *fintech interview challenge*, and fully refactored in **2025** to demonstrate modern backend engineering practices — including concurrency safety, transactional integrity, clean architecture, and robust testing.

This repository serves as a **professional engineering case study**, showcasing how a legacy codebase can evolve into a maintainable, testable, and production-grade backend service.

---

# 📘 Overview

The original 2019 implementation met functional requirements but exhibited several architectural and concurrency limitations commonly seen in early-stage backend systems:

- No thread-safety or state isolation
- Race conditions in financial operations
- Missing transactional guarantees
- Weak test coverage
- Tight coupling between DAO and business logic

In **2025**, the project was rebuilt from the ground up using Java 21 and clean architectural principles.

---

# 🛠️ Evolution of This Project

### 📅 2019 — Initial Implementation
A functional banking API built with pure Java, focusing on fundamentals and correctness. However, several issues were identified:

- Shared, thread-unsafe JDBC connection
- No rollback handling for multi-step operations
- Race conditions on deposits, withdrawals, and transfers
- Lack of modularity
- Missing integration and concurrency tests

---

### 🚀 2025 — Full Refactor
Drawing on years of experience in mission-critical fintech and distributed systems, the project was revisited to align it with **senior-level engineering standards**.

**Refactor Objectives:**

1. Ensure concurrency safety and proper synchronization
2. Introduce pessimistic locking (`SELECT FOR UPDATE`)
3. Improve separation of concerns and domain structure
4. Add integration and concurrency test suites
5. Modernize codebase with Java 21
6. Provide a production-ready project layout

---

# ✨ Features

- Lightweight REST API using Undertow
- Thread-safe financial operations
- Pessimistic row locking for isolation
- Transactional rollback for multi-step workflows
- Clean layered architecture (Controller → Service → DAO)
- H2 in-memory database for reproducible tests
- Modern Java 21 language features

---

# 🧱 Tech Stack

- Java 21
- Undertow
- JDBC + H2
- Gradle
- JUnit 5 · REST Assured
- Docker

---

# ▶️ Running the Application

## Option A — Local Execution

```
./gradlew clean build
java -jar build/libs/banking-account.jar
```

Access:

```
http://localhost:8080
```

---

## Option B — Docker

```
docker build -t banking-account:latest .
docker run -d -p 8080:8080 banking-account:latest
```

---

# 🧪 Running Tests

```
./gradlew test
./gradlew integrationTest
```

---

# 🔍 Code Quality — Quick Reference

This project includes a lightweight, production-style quality pipeline designed to keep the codebase clean, safe, and maintainable.

## 🚀 Why This Matters
Linting enforces:

- Consistent formatting
- Safe coding patterns
- Reduced complexity
- High maintainability

These practices reflect standards used in fintech and high-availability backend systems.

## ⚙️ Tools
- **Spotless** — formatting (Google Java Style)
- **Checkstyle** — static analysis & best practices
- **EditorConfig** — editor-agnostic consistency

## 🧭 Most Important Commands

**Format code (auto-fix)**
```
./gradlew spotlessApply
```

**Verify formatting**
```
./gradlew spotlessCheck
```

**Run static analysis**
```
./gradlew checkstyleMain checkstyleTest
```

**Full quality gate (CI equivalent)**
```
./gradlew check
```

## 📊 Reports
- Checkstyle: `build/reports/checkstyle/checkstyle.html`
- Tests: `build/reports/tests/`

---

# 📐 Architecture Overview

```
REST Controller
        ↓
  Service Layer
        ↓
       DAO
        ↓
    H2 Database
```

---

# 🤝 Contributing
Contributions and suggestions are welcome.

---

# ⭐ Final Notes
This repository illustrates not only how software evolves —  
but how an **engineer evolves**.
