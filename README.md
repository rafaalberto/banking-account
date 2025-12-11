# 🏦 Banking Account API — Refactored in 2025

[![CI](https://github.com/rafaalberto/banking-account/actions/workflows/ci.yml/badge.svg)](https://github.com/rafaalberto/banking-account/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

A lightweight, framework-free banking API originally developed in **2019** for a *fintech interview challenge*, and fully refactored in **2025** to demonstrate modern backend engineering practices — including concurrency safety, transactional integrity, clean architecture, and robust testing.

This repository serves as a **professional engineering case study**, showing how a legacy codebase evolves into a maintainable, testable, and production-grade backend service.

---

# 📘 Overview

The original 2019 implementation met functional requirements but had several architectural and concurrency limitations typical of early-stage backend designs:

- No thread-safety or state isolation
- Race conditions in financial operations
- Lack of transactional guarantees
- Weak test coverage
- DAO tightly coupled with business logic

In **2025**, this project was rebuilt from the ground up using Java 21 and clean architectural principles.

---

# 🛠️ Evolution of This Project

### 📅 2019 — Initial Implementation
A functional banking API built without heavy frameworks, focusing on Java fundamentals and correctness. However, several issues existed:

- Shared, thread-unsafe JDBC connection
- Missing transactional rollback on multi-step operations
- Race conditions on deposits, withdrawals, and transfers
- Architecture lacking modularity
- No integration or concurrency tests

---

### 🚀 2025 — Full Refactor

After years working on mission-critical fintech systems and distributed architectures, the project was revisited with the goal of elevating it to **senior-level engineering standards**.

**Refactor Objectives:**

1. Enforce concurrency safety and proper synchronization
2. Introduce pessimistic locking with `SELECT FOR UPDATE`
3. Improve separation of concerns and domain structure
4. Add integration + concurrency test suites
5. Modernize with Java 21 features
6. Provide production-quality project organization

This repository now represents both a **technical showcase** and an **educational platform**.

---

# ✨ Features

- Lightweight REST API using Undertow (no heavy frameworks)
- Thread-safe financial operations
- Pessimistic locking for isolation
- Transactional rollback for multi-step flows
- Clean layering: Controller → Service → DAO
- H2 in-memory database for fast, reliable testing
- Java 21 modern syntax and patterns

---

# 🎯 Project Goals (2025)

- Transform the 2019 codebase using modern engineering
- Resolve concurrency issues at the architectural level
- Improve domain structure and separation of responsibilities
- Deliver robust testing with integration + concurrency focus
- Provide a template-grade backend service

---

# 🧱 Tech Stack

- **Java 21**
- **Undertow**
- **JDBC + H2 Database**
- **Gradle**
- **JUnit 5** · **REST Assured**
- **Docker** (optional)

---

# 🔄 Refactor 2025 — Detailed Improvements

### 🚨 Issues Identified (2019)
- Race conditions in financial operations
- Singleton shared connection (thread-unsafe)
- No rollback / partial state commits
- Business logic leaking into DAO layer
- Weak synchronization strategy
- Missing integration and concurrency testing

---

### 🛠 Fixes Implemented
- ✔ Migrated to Java 21
- ✔ Added connection pooling
- ✔ Implemented pessimistic row locking
- ✔ Refactored layers into clean architecture
- ✔ Added integration + concurrency test suites
- ✔ Improved documentation and structure

---

### 🔮 Upcoming
- Structured logging
- Custom exception hierarchy
- Immutable `Account` domain model

---

# ▶️ Running the Application

## Option A — Run Locally

Build:
```bash
./gradlew clean build
```

Run:
```bash
java -jar build/libs/banking-account.jar
```

Access:
```
http://localhost:8080
```

---

## Option B — Run with Docker (Recommended)

Build:
```bash
docker build -t banking-account:latest .
```

Run:
```bash
docker run -d -p 8080:8080 --name banking-account banking-account:latest
```

---

# 🧪 Running Tests
```bash
./gradlew test
./gradlew integrationTest
```

---

# 🔍 Code Quality & Linting

This project uses a multi-tool approach to ensure code quality and consistency:

## Tools

- **Spotless** (Google Java Format) - Automatic code formatting
- **Checkstyle** - Static code analysis and quality checks
- **EditorConfig** - Editor-agnostic formatting rules

## Workflow

### Before Committing

1. **Format your code:**
   ```bash
   ./gradlew spotlessApply
   ```
   This will automatically format your code according to Google Java Format standards.

2. **Check for issues:**
   ```bash
   ./gradlew check
   ```
   This runs:
   - `spotlessCheck` - Verifies code formatting
   - `checkstyleMain` & `checkstyleTest` - Static analysis
   - `test` & `integrationTest` - All tests

### Individual Checks

```bash
# Check formatting only
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply

# Check code quality only
./gradlew checkstyleMain checkstyleTest
```

### View Reports

- **Checkstyle HTML Report:** `build/reports/checkstyle/checkstyle.html`
- **Test Reports:** `build/reports/tests/`

## Configuration

- **Checkstyle:** `config/checkstyle/checkstyle.xml`
  - Focuses on code quality (complexity, naming, best practices)
  - Style formatting is handled by Spotless to avoid conflicts

- **Spotless:** Configured in `build.gradle`
  - Uses Google Java Format 1.17.0
  - Removes unused imports
  - Trims trailing whitespace
  - Ensures newline at end of file

- **EditorConfig:** `.editorconfig`
  - Ensures consistent editor settings across IDEs
  - 120 character line length
  - 4-space indentation for Java

## CI/CD Integration

The build will **fail** if:
- Code is not properly formatted (Spotless)
- Code quality violations are found (Checkstyle)
- Tests fail

This ensures all code meets quality standards before merging.

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

A minimal, clean architecture focused on readability, correctness, and maintainability.

---

# 🤝 Contributing
Suggestions and contributions are welcome as development continues.

---

# ⭐ Final Notes

This repository demonstrates not only how a project evolves —  
but how an **engineer evolves**.

More improvements coming soon.
