# Best Practices: Unit vs Integration Test Organization

## Overview

There are several common approaches to organizing unit and integration tests. This guide covers the most widely adopted patterns.

---

## Approach 1: Package-Based Separation (Recommended for Gradle/Java)

This is the **most common approach** and works well with Gradle test configuration.

### Structure:
```
src/
├── main/
│   └── java/com/api/account/
│       ├── resource/
│       ├── service/
│       └── repository/
└── test/
    └── java/com/api/account/
        ├── unit/              # Unit tests only
        │   ├── resource/
        │   ├── service/
        │   └── repository/
        └── integration/       # Integration tests only
            ├── resource/
            ├── service/
            └── repository/
```

### Naming Convention:
- **Unit tests**: `*Test.java` or `*UnitTest.java`
- **Integration tests**: `*IT.java` or `*IntegrationTest.java`

### Example:
```
src/test/java/com/api/account/
├── unit/
│   ├── service/
│   │   └── AccountServiceImplTest.java       # Mocks dependencies
│   └── repository/
│       └── AccountDaoImplTest.java           # In-memory database
└── integration/
    ├── resource/
    │   └── AccountResourceIT.java            # Full HTTP + Database
    └── service/
        └── AccountServiceIntegrationTest.java # Real database
```

---

## Approach 2: Separate Source Sets (Advanced - For Large Projects)

Use Gradle source sets to completely separate unit and integration tests.

### Structure:
```
src/
├── main/java/
├── test/java/           # Unit tests
└── integrationTest/java/ # Integration tests
```

### build.gradle Configuration:
```gradle
sourceSets {
    integrationTest {
        java {
            compileClasspath += main.output + test.output
            runtimeClasspath += main.output + test.output
            srcDir file('src/integrationTest/java')
        }
        resources.srcDir file('src/integrationTest/resources')
    }
}

configurations {
    integrationTestImplementation.extendsFrom testImplementation
    integrationTestRuntimeOnly.extendsFrom testRuntimeOnly
}

task integrationTest(type: Test) {
    testClassesDirs = sourceSets.integrationTest.output.classesDirs
    classpath = sourceSets.integrationTest.runtimeClasspath
    useJUnitPlatform()
}

check.dependsOn integrationTest
```

---

## Approach 3: Tag-Based (JUnit 5 - Recommended for Flexibility)

Use JUnit 5 tags to mark tests, keeping them in the same package structure as production code.

### Structure (Mirrors main):
```
src/
├── main/java/com/api/account/
│   └── service/
│       └── AccountService.java
└── test/java/com/api/account/
    └── service/
        └── AccountServiceTest.java    # Contains both unit & integration
```

### Example Test:
```java
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class AccountServiceTest {
    
    @Test
    @Tag("unit")
    void shouldCreateAccount() {
        // Unit test with mocks
    }
    
    @Test
    @Tag("integration")
    void shouldCreateAccountInDatabase() {
        // Integration test with real database
    }
}
```

### build.gradle Configuration:
```gradle
tasks.withType(Test).configureEach {
    useJUnitPlatform()
}

task unitTest(type: Test) {
    useJUnitPlatform {
        includeTags("unit")
        excludeTags("integration")
    }
}

task integrationTest(type: Test) {
    useJUnitPlatform {
        includeTags("integration")
        excludeTags("unit")
    }
}
```

---

## Recommended Approach for Your Project

Based on your current structure, I recommend **Approach 1 (Package-Based)** with improvements:

### Structure:
```
src/test/java/com/api/account/
├── unit/
│   ├── service/
│   │   └── impl/
│   │       ├── AccountServiceImplTest.java    ✅ Pure unit (mocked)
│   │       └── TransactionServiceImplTest.java ✅ Pure unit (mocked)
│   └── repository/
│       └── impl/
│           └── AccountDaoImplTest.java        ⚠️ Might be integration (uses DB)
│
└── integration/
    ├── resource/
    │   ├── AccountResourceIT.java            ✅ Full integration
    │   └── TransactionResourceIT.java        ✅ Full integration
    └── service/
        └── AccountServiceIntegrationTest.java (if needed)
```

### Naming Convention:
- **Unit tests**: Keep as `*Test.java` or use `*UnitTest.java`
- **Integration tests**: Use `*IT.java` suffix (common convention)

---

## Gradle Configuration for Separate Test Execution

Add this to your `build.gradle` to run tests separately:

```gradle
// Configure unit tests (exclude integration tests)
test {
    useJUnitPlatform {
        excludeTags("integration")
        // Or use include pattern:
        // include "**/*Test.java"
        // exclude "**/*IT.java", "**/*IntegrationTest.java"
    }
}

// Task for integration tests only
task integrationTest(type: Test) {
    useJUnitPlatform {
        includeTags("integration")
        // Or use include pattern:
        // include "**/*IT.java", "**/*IntegrationTest.java"
    }
    shouldRunAfter test
}

// Run both in check task
check.dependsOn integrationTest
```

### Alternative: Pattern-Based (No Tags Needed)

```gradle
test {
    useJUnitPlatform()
    exclude "**/integration/**"
    exclude "**/*IT.java"
    exclude "**/*IntegrationTest.java"
}

task integrationTest(type: Test) {
    useJUnitPlatform()
    include "**/integration/**"
    include "**/*IT.java"
    include "**/*IntegrationTest.java"
    shouldRunAfter test
}
```

---

## When to Use Each Approach

### Use Package-Based (Approach 1) When:
- ✅ Clear separation needed between unit and integration tests
- ✅ Different test execution times (fast unit tests, slow integration)
- ✅ Different CI/CD pipeline stages
- ✅ **Your current project size** (recommended for you)

### Use Separate Source Sets (Approach 2) When:
- ✅ Large enterprise projects
- ✅ Need different dependencies for integration tests
- ✅ Need completely separate test configurations
- ✅ Multiple test types (unit, integration, e2e)

### Use Tag-Based (Approach 3) When:
- ✅ Tests are closely related to production code structure
- ✅ Want flexibility to categorize tests differently
- ✅ Don't want to duplicate package structure
- ✅ Need fine-grained control (e.g., `@Slow`, `@Fast`, `@Database`)

---

## Current Issues in Your Structure

Looking at your `AccountResourceTest.java`:

⚠️ **Issue**: It's in `unit/` but uses:
- Real `DatabaseConnection`
- Real `AccountDaoImpl` 
- Real `Undertow` server
- Real HTTP calls

This is actually an **integration test**, not a unit test!

### Fix Options:

**Option 1: Move to integration folder**
```
src/test/java/com/api/account/
└── integration/
    └── resource/
        └── AccountResourceIT.java  # Rename with IT suffix
```

**Option 2: Create true unit test (if needed)**
```java
// Unit test - mocks everything
@ExtendWith(MockitoExtension.class)
class AccountResourceTest {
    @Mock
    private AccountService accountService;
    
    @InjectMocks
    private AccountResource accountResource;
    
    // Test with mocks only
}
```

---

## Recommended Structure for Your Project

```
src/test/java/com/api/account/
├── unit/
│   ├── service/
│   │   └── impl/
│   │       ├── AccountServiceImplTest.java        ✅ (already correct)
│   │       └── TransactionServiceImplTest.java    ✅ (already correct)
│   └── repository/
│       └── impl/
│           └── AccountDaoImplTest.java            ⚠️ Check if it uses real DB
│
└── integration/
    ├── resource/
    │   ├── AccountResourceIT.java                 ⬅️ Move from unit/
    │   └── TransactionResourceIT.java             ⬅️ Move from unit/
    └── service/
        └── (add if needed for service-level integration)
```

---

## Best Practices Summary

### ✅ DO:
- **Separate by package**: `unit/` vs `integration/`
- **Use naming convention**: `*IT.java` for integration tests
- **Keep unit tests fast**: Mock external dependencies
- **Run unit tests frequently**: In CI on every commit
- **Run integration tests less frequently**: In CI on PR/release

### ❌ DON'T:
- Mix unit and integration tests in same file without tags
- Put integration tests in `unit/` folder
- Use real database/external services in unit tests
- Make unit tests depend on integration tests

---

## Quick Commands

After setup, you can run:

```bash
# Run only unit tests (fast)
./gradlew test

# Run only integration tests (slower)
./gradlew integrationTest

# Run all tests
./gradlew check
```

---

## Migration Steps for Your Project

1. ✅ You already have `unit/` and `integration/` folders - good!
2. ⚠️ Move `AccountResourceTest.java` and `TransactionResourceIT.java` to `integration/resource/`
3. ✅ Rename integration tests to `*IT.java` suffix
4. ✅ Add Gradle configuration to run tests separately
5. ✅ Verify `AccountDaoImplTest.java` - if it uses real DB, move to integration

---

## References

- [JUnit 5 Tags Documentation](https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering)
- [Gradle Test Configuration](https://docs.gradle.org/current/userguide/java_testing.html)
- [Maven Surefire vs Failsafe](https://maven.apache.org/surefire/maven-surefire-plugin/)(for Maven projects)

