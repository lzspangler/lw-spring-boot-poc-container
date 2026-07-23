# Spring Boot Lightwell FSI Demo

A **retail banking** proof-of-concept on **Spring Boot 2.7.18** and **Java 17** that demonstrates how
[Red Hat Lightwell Network](https://docs.redhat.com/en/documentation/red_hat_lightwell_network/)
remediated dependency patching eliminates CVEs — without upgrading Spring Boot.

| Property | Value |
|----------|-------|
| Spring Boot | 2.7.18 |
| Java | 17 |
| Build tool | Maven |
| GroupId | `com.redhat.lightwell` |
| Artifact | `spring-boot-lw-poc-0.0.1-SNAPSHOT.jar` |

## Purpose

Financial institutions run mission-critical systems on Spring Boot 2.7.x, which reached
end of open-source support in November 2023. Upgrading to Spring Boot 3.x requires a
`javax.*` to `jakarta.*` namespace migration, extensive regression testing, and
re-certification with auditors (PCI-DSS, SOC 2, SOX) — typically a 6-to-18-month effort.

This project provides a realistic FSI workload that exercises 10 vulnerable transitive
dependencies. Two scripts switch between upstream (vulnerable) and Lightwell remediated
versions of `pom.xml`, enabling a side-by-side CVE comparison using the
[Red Hat Dependency Analytics](https://marketplace.visualstudio.com/items?itemName=redhat.fabric8-analytics)
VSCode extension.

## Prerequisites

1. **Java 17** and **Maven 3.8+**
2. **Lightwell credentials** exported as environment variables:
   ```bash
   export LW_USERNAME="REPLACE_WITH_YOUR_LW_USERNAME"
   export LW_PASSWORD="REPLACE_WITH_YOUR_LW_PASSWORD"
   ```
3. Always pass the project `settings.xml`:
   ```bash
   mvn -s settings.xml <goal>
   ```

> If you have a local Nexus instance, use `settings-nexus.xml`. For JFrog Artifactory,
> use `settings-artifactory.xml`.

## Quick Start

```bash
mvn -s settings.xml clean verify          # build and run all 121 tests
mvn -s settings.xml spring-boot:run       # run locally on port 8080
```

Swagger UI: <http://localhost:8080/swagger-ui.html>

### Sample API calls

```bash
# List customers
curl -s -u user:user123 http://localhost:8080/api/customers | jq '.[0:2]'

# Check account balance
curl -s -u user:user123 http://localhost:8080/api/accounts/1/balance | jq

# Transfer funds
curl -s -u user:user123 -X POST http://localhost:8080/api/transfers \
  -H 'Content-Type: application/json' \
  -d '{"sourceAccountId":1,"targetAccountId":2,"amount":100.00,"description":"Demo transfer"}' | jq

# Credit check (exercises Apache HttpClient)
curl -s -u user:user123 -X POST http://localhost:8080/api/customers/1/credit-check | jq

# XML statement import (exercises Woodstox)
curl -s -u admin:admin123 -X POST http://localhost:8080/api/accounts/1/import-statement \
  -H 'Content-Type: application/xml' \
  -d '<statement><transaction><type>DEPOSIT</type><amount>500.00</amount><description>Wire in</description></transaction></statement>' | jq

# Admin YAML config import (exercises SnakeYAML)
curl -s -u admin:admin123 -X POST http://localhost:8080/api/admin/config \
  -H 'Content-Type: text/x-yaml' \
  -d 'risk-thresholds:
  low: 720
  medium: 650' | jq

# Admin JSON query (exercises json-smart)
curl -s -u admin:admin123 -X POST http://localhost:8080/api/admin/query \
  -H 'Content-Type: application/json' \
  -d '{"data":"{\"total\":8,\"active\":8}","expression":"$.total"}' | jq
```

## FSI Banking Features

Each feature exercises a specific vulnerable dependency that Lightwell remediates:

| Feature | Endpoint | Vulnerable Dependency |
|---------|----------|-----------------------|
| Customer & account management | `/api/customers`, `/api/accounts` | Spring Boot, Spring Security, Spring Data JPA |
| Fund transfers & transactions | `/api/transfers`, `/api/accounts/{id}/transactions` | Spring Boot (validation, tx management) |
| Document upload & storage | `/api/accounts/{id}/documents` | Apache Commons IO, Commons FileUpload |
| Credit bureau checks | `/api/customers/{id}/credit-check` | Apache HttpClient 4.x |
| XML bank statement import | `/api/accounts/{id}/import-statement` | Woodstox (StAX XML parser) |
| Admin JSON data queries | `/api/admin/query` | json-smart |
| YAML configuration import | `/api/admin/config` | SnakeYAML |
| Audit logging | All write operations | Logback |
| In-memory database | All data persistence | H2 |
| Role-based access control | `/api/admin/**` restricted to ADMIN | Spring Security (RBAC) |

### Security model

Two in-memory users are configured for the demo:

| User | Password | Role | Access |
|------|----------|------|--------|
| `admin` | `admin123` | ADMIN | All endpoints |
| `user` | `user123` | USER | `/api/**` except `/api/admin/**` |

## Demo Flow

The demo is designed as a 15-minute walkthrough using VSCode and the Red Hat Dependency
Analytics extension. Full presenter materials are in the `docs/` directory.

### 1. Show the vulnerable state

Start with upstream dependency versions (the default state of `pom.xml`):

```bash
./revert-rhlw-version.sh    # ensure clean "before" state
mvn -s settings.xml clean verify
```

Open `pom.xml` in VSCode, right-click, and run **Red Hat Dependency Analytics Report**.
The scan will flag CVEs across Spring Boot, SnakeYAML, HttpClient, Woodstox, json-smart,
Logback, H2, Spring Security, Commons IO, and Commons FileUpload.

### 2. Apply Lightwell remediation

```bash
./apply-rhlw-version.sh
```

This updates `pom.xml` in-place, adding `.rhlw-*` suffixes to all dependency versions.
No application code changes — only Maven artifact coordinates change.

### 3. Show the remediated state

```bash
mvn -s settings.xml clean verify -U
```

All 121 tests still pass. Re-run the Dependency Analytics scan to show the CVE reduction.

### 4. Revert (optional)

```bash
./revert-rhlw-version.sh
```

The scripts are idempotent — running either one multiple times produces the same result.

### Presenter materials

| Document | Description |
|----------|-------------|
| [docs/SLIDE-OUTLINE.md](./docs/SLIDE-OUTLINE.md) | 12-slide presentation with appendices on CVEs and FSI regulations |
| [docs/DEMO-SCRIPT.md](./docs/DEMO-SCRIPT.md) | Step-by-step 15-minute demo script with talking points and troubleshooting |

## Dependencies Remediated

| Dependency | Upstream Version | Lightwell Version | Key CVEs Addressed |
|------------|-----------------|--------------------|--------------------|
| Spring Boot (all starters) | 2.7.18 | 2.7.18.rhlw-00004 | Multiple Spring Framework CVEs |
| Spring Security Core | 5.7.11 | 5.7.11.rhlw-00006 | CVE-2025-22235 (authorization bypass) |
| SnakeYAML | 1.33 | 1.33.0.rhlw-00001 | CVE-2022-1471 (RCE via deserialization) |
| Logback Classic | 1.2.11 | 1.2.11.rhlw-00001 | CVE-2023-6378 (DoS) |
| Apache Commons FileUpload | 1.4 | 1.4.0.rhlw-00001 | CVE-2023-24998 (DoS) |
| Apache Commons IO | 2.11.0 | 2.11.0.rhlw-00001 | CVE-2024-47554 (path traversal) |
| Apache HttpClient | 4.5.12 | 4.5.12.rhlw-00001 | CVE-2023-45648 (request smuggling) |
| Woodstox Core | 6.0.3 | 6.0.3.rhlw-00001 | CVE-2022-40152 (XXE) |
| json-smart | 2.4.8 | 2.4.8.rhlw-00001 | CVE-2023-1370 (stack overflow) |
| H2 Database | 2.1.214 | 2.2.219.rhlw-00001 | CVE-2022-45868 (RCE) |

## Test Coverage

The project includes 121 tests across three tiers, measured by JaCoCo:

| Metric | Value | Target |
|--------|-------|--------|
| Total tests | 121 | — |
| Line coverage | 95.1% | > 90% |
| Branch coverage | 78.1% | > 75% |

### Test breakdown

| Tier | Count | Description |
|------|-------|-------------|
| Unit tests | ~45 | `@ExtendWith(MockitoExtension.class)` — isolated service logic with mocked dependencies |
| Integration tests | ~71 | `@SpringBootTest` + `@AutoConfigureMockMvc` — full stack through HTTP with H2 database |
| Regression tests | 5 | `BankingWorkflowRegressionTest` — end-to-end multi-step banking workflows |

### Service coverage

| Service | Line Coverage | Branch Coverage |
|---------|--------------|-----------------|
| ConfigImportService | high | 100% |
| DocumentService | high | 100% |
| StatementImportService | high | 89% |
| TransactionService | high | 83% |
| DataQueryService | high | 78% |
| AuditService | high | 75% |
| CustomerService | high | 71% |
| AccountService | high | 50% |
| CreditCheckService | moderate | 21% |

> CreditCheckService has lower branch coverage because it creates an HTTP client
> internally (`HttpClients.createDefault()`), making the success path difficult to
> unit test without refactoring. The fallback path is fully covered.

To generate the coverage report locally:

```bash
mvn -s settings.xml clean verify
open target/site/jacoco/index.html
```

## Application Components

| Path | Description |
|------|-------------|
| `pom.xml` | Maven build with all dependencies and JaCoCo coverage |
| `settings.xml` | Lightwell Maven repository credentials (`-s settings.xml`) |
| `apply-rhlw-version.sh` | Switch pom.xml to Lightwell remediated versions |
| `revert-rhlw-version.sh` | Revert pom.xml to upstream vulnerable versions |
| `openapi/openapi.yaml` | OpenAPI 3 contract |
| `src/main/java/com/redhat/lightwell/` | Application source |
| `src/test/java/com/redhat/lightwell/` | Unit, integration, and regression tests |

### Dependency tree and SBOM

```bash
mvn -s settings.xml dependency:tree > target/dependency-tree.txt

syft ./target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar \
  -o cyclonedx-json=target/spring-boot-lw-poc-0.0.1-sbom.json
```

## Repository Documentation

| Path | Description |
|------|-------------|
| [AGENTS.md](./AGENTS.md) | Instructions for AI coding agents |
| [docs/SLIDE-OUTLINE.md](./docs/SLIDE-OUTLINE.md) | Demo slide deck outline |
| [docs/DEMO-SCRIPT.md](./docs/DEMO-SCRIPT.md) | Step-by-step demo walkthrough |
| [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) | Architecture and API-first design |
| [docs/API.md](./docs/API.md) | API-first workflow and samples |
| [docs/BUILD-AND-RUN.md](./docs/BUILD-AND-RUN.md) | Build, credentials, TLS, and troubleshooting |
| [docs/TESTING.md](./docs/TESTING.md) | Testing strategy and practices |

## External References

- [Red Hat Lightwell Network Documentation](https://docs.redhat.com/en/documentation/red_hat_lightwell_network/)
- [Red Hat Dependency Analytics — VSCode Extension](https://marketplace.visualstudio.com/items?itemName=redhat.fabric8-analytics)
- [Spring Boot 2.7 Reference Documentation](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/)
