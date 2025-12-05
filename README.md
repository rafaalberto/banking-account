# Banking Account API — Refactored in 2025

## Status & Technology Badges
[![CI](https://github.com/rafaalberto/banking-account/actions/workflows/ci.yml/badge.svg)](https://github.com/rafaalberto/banking-account/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Build](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Overview

This project was originally developed in **2019** as part of a coding challenge for **Revolut**.  
In **2025**, it is undergoing a full refactor focused on concurrency safety, transactional integrity, clean architecture, and robust testing.

This repository serves as both an **engineering case study** and a **progressive refactor**, showcasing how a legacy codebase evolves into a modern and production-ready design.

---

## ✨ Features

- Lightweight REST API using Undertow (no heavy frameworks)
- Thread-safe handling of financial operations
- Proper transactional boundaries with rollback
- In-memory H2 database for fast integration testing
- SOLID principles
- Full refactor moving toward dependency injection and structured logging

---

## 🎯 Project Goals (2025)

- Modernize the 2019 codebase using Java 21
- Introduce concurrency-safe transaction handling
- Improve separation of concerns and eliminate architecture anti-patterns
- Provide comprehensive test coverage
- Deliver a template-quality codebase for educational and professional purposes

---

## 🧱 Tech Stack

- **Java 21**
- **Undertow (Embedded Web Server)**
- **JDBC + H2 Database**
- **Gradle**
- **JUnit 5**, **REST Assured**
- **Docker** (optional execution)

---

## 🔄 Refactor 2025 — Improvements

### Key Issues Identified in the 2019 Version
- Race conditions in transfers, deposits, and withdrawals
- Shared singleton JDBC connection (thread-unsafe)
- Missing rollback in multi-step operations
- DAO violating separation of concerns
- Poor locking strategy (`synchronized(this)`)
- No concurrency or integration testing

### Fixes Implemented in 2025 *(WIP)*
- Upgraded to **Java 21**
- Improved foundation for dependency injection
- Introduced connection pooling
- Added pessimistic row locking via `SELECT FOR UPDATE`
- Extracted DAO responsibilities
- Added project documentation
- Added integration + concurrency tests

### Upcoming Improvements
- Add structured logs and exception hierarchy
- The `Account` entity will be refactored into an **immutable model**.

---

## ▶ Running the Application

You may run the application **locally** or through **Docker**.  
Docker is the recommended approach for consistency and portability.

---

### Option A — Run Locally

Build:

```bash
./gradlew clean build
```

Run:

```bash
java -jar build/libs/banking-account.jar
```

Access the API at:

```
http://localhost:8080
```

---

### Option B — Run with Docker (Recommended)

#### 1) Build the Docker Image

```bash
docker build -t banking-account:latest .
```

Optional semantic versioning:

```bash
docker build -t banking-account:1.0.0 .
```

---

#### 2) Run the Container

```bash
docker run -d -p 8080:8080 --name banking-account banking-account:latest
```

---

## 🧪 Running Tests

```bash
./gradlew test
./gradlew integrationTest
```

---

## 📐 Architecture Overview

```
REST Controller → Service Layer → DAO → H2 Database
```

---

## 🤝 Contributing

Contributions and suggestions are welcome as development continues.

---

## ⭐ Final Notes

This repository serves as both an educational reference and an evolving engineering case study.
Additional improvements will be introduced progressively as the refactor advances.
