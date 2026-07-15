# 🧪 Testing practices

Testing standards for the Spring Boot LW POC (Java 17, Spring Boot 2.7.18, Maven).

## 🎯 Goals

- Catch regressions early with fast, reliable automated tests
- Keep tests readable and aligned with production package structure
- Prefer testing behavior over implementation details

## 🧰 Tooling

Spring Boot 2.7 defaults (via `spring-boot-starter-test`):

| Tool | Purpose |
|------|---------|
| JUnit 5 (Jupiter) | Test framework |
| Mockito | Mocking dependencies |
| AssertJ | Fluent assertions (optional but recommended) |
| Spring Boot Test | `@SpringBootTest`, `@WebMvcTest`, test slices |
| MockMvc | Controller layer HTTP tests |

## 📁 Test layout

Mirror the main source tree under `src/test/java/`:

```
src/test/java/com/redhat/lightwell/
├── ApplicationTest.java              # @SpringBootTest context load
├── controller/GreetingControllerTest.java  # @WebMvcTest
└── service/GreetingServiceTest.java        # Unit tests
```

Test resources (if needed): `src/test/resources/application.properties`

## 🧩 Test types

### 🔬 Unit tests

- **Scope**: Single class, dependencies mocked
- **Annotation**: None required (plain JUnit 5) or `@ExtendWith(MockitoExtension.class)`
- **Speed**: Must run in milliseconds
- **Naming**: `<ClassUnderTest>Test.java` (e.g. `ItemServiceTest.java`)

Example method naming: `shouldReturnItemWhenIdExists()`, `shouldThrowWhenIdNotFound()`

### 🔗 Slice / integration tests

- **Controller**: `@WebMvcTest(ControllerClass.class)` + `MockMvc`
- **Full context**: `@SpringBootTest` — use sparingly; slower startup
- **Persistence** (when added): `@DataJpaTest` for repository layer

Prefer the **narrowest test slice** that validates the behavior.

### 🌐 End-to-end tests

Not required for the initial POC unless explicitly requested. If added, document tooling and scope here.

## ▶️ Running tests

```bash
# All tests
mvn -s settings.xml test

# Single test class
mvn -s settings.xml test -Dtest=ItemServiceTest

# Single test method
mvn -s settings.xml test -Dtest=ItemServiceTest#shouldReturnItemWhenIdExists

# Build including tests (recommended before commit)
mvn -s settings.xml clean verify
```

## 📏 Conventions

1. **Arrange–Act–Assert** — Structure each test method clearly.
2. **One logical assertion per test** — Multiple asserts on the same outcome are acceptable.
3. **No test order dependency** — Tests must pass in isolation and in any order.
4. **Avoid `@SpringBootTest` for pure unit tests** — Do not load the full application context unnecessarily.
5. **Test data** — Use builders or small factory methods; avoid large shared fixtures unless justified.
6. **Do not test framework code** — Do not assert Spring or third-party library internals.

## 📊 Coverage expectations

- **Services and domain logic**: High coverage expected
- **Controllers**: Cover success and main error paths via MockMvc
- **Repositories**: Cover custom queries when introduced
- **Trivial getters/setters**: No dedicated tests required

Coverage plugins may be added later; until then, focus on meaningful tests over percentage targets.

## 🔄 Continuous integration

When CI is configured, the canonical command is:

```bash
mvn -s settings.xml clean verify
```

CI and local development must use the same Maven goals to avoid drift.

## 🔗 Related documents

- [Architecture](./ARCHITECTURE.md)
- [Build and run](./BUILD-AND-RUN.md)
- [Agent guide (AGENTS.md)](../AGENTS.md)
