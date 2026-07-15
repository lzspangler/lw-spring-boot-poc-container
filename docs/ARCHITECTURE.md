# 🏛️ Architecture

Architecture of the Spring Boot LW POC application.

## 🔭 Overview

The application follows a **classic layered architecture** aligned with Spring Boot conventions:

```
┌─────────────────────────────────────────┐
│  Controller layer  (HTTP / REST API)    │
├─────────────────────────────────────────┤
│  Service layer     (business logic)     │
├─────────────────────────────────────────┤
│  Repository layer (data access)         │
├─────────────────────────────────────────┤
│  Model             (entities, DTOs)     │
└─────────────────────────────────────────┘
```

Each layer has a single responsibility and depends only on layers below it.

## 🌐 API-first design

Public HTTP APIs are defined **before** implementation. The contract drives code generation and
manual alignment of controllers and DTOs.

```
openapi/openapi.yaml          ← single source of truth (paths, schemas, examples)
        │
        ▼
OpenAPI Generator (Maven)     ← scaffold interfaces / models on first add or contract change
        │
        ▼
Controller + DTO              ← thin HTTP layer; matches operationId and schemas
        │
        ▼
Service                       ← business logic (hand-written)
        │
        ▼
Model / repository            ← DTOs and persistence as needed
```

| Step | Action |
|------|--------|
| 1 | Edit [openapi/openapi.yaml](../openapi/openapi.yaml) — add or change operations and examples |
| 2 | Update [API.md](./API.md) — human workflow and curl samples |
| 3 | Run `mvn -s settings.xml openapi-generator:generate` — generate or refresh API artifacts |
| 4 | Implement or adjust services and wire controllers to match the contract |
| 5 | Verify with tests and samples; run `mvn -s settings.xml clean verify` |

Contract reference: [API.md](./API.md). Conventions: [CONVENTIONS.md](./CONVENTIONS.md).

## 📦 Lightwell dependency resolution

Dependencies are resolved from **Red Hat Lightwell Network** Maven repositories configured in
`settings.xml` at the repository root.

| Profile | URL suffix | Typical use |
|---------|------------|-------------|
| `lightwell-validated` | `/java/validated` | Standard validated artifacts |
| `lightwell-remediated` | `/java/remediated` | Remediated versions (e.g. `2.7.18.rhlw-00003`) |

Authentication uses environment variables `LW_USERNAME` and `LW_PASSWORD` (never committed).
Remediated dependency versions in `pom.xml` are commented by default; uncomment when pulling from
the remediated stream.

Build and credential setup: [BUILD-AND-RUN.md](./BUILD-AND-RUN.md).

## 🧰 Technology stack

| Layer / concern | Technology |
|-----------------|------------|
| Runtime | Java 17 |
| Framework | Spring Boot 2.7.18 |
| Web | Spring Web (`spring-boot-starter-web`) |
| Validation | Spring Validation (`spring-boot-starter-validation`) |
| Build | Maven |
| Dependency repos | Red Hat Lightwell (`settings.xml` profiles) |
| API contract | OpenAPI 3 (`openapi/openapi.yaml`) |
| API documentation UI | springdoc-openapi (Swagger UI) |
| Code generation | OpenAPI Generator Maven plugin |
| Testing | JUnit 5, Spring Boot Test, Mockito |

## 📦 Package structure

Maven groupId: `com.redhat.lightwell`. Base package: `com.redhat.lightwell`

| Package | Responsibility |
|---------|----------------|
| `com.redhat.lightwell` | `@SpringBootApplication` main class |
| `com.redhat.lightwell.config` | `@Configuration`, beans, property binding |
| `com.redhat.lightwell.controller` | REST endpoints, request/response mapping |
| `com.redhat.lightwell.service` | Business rules and orchestration |
| `com.redhat.lightwell.repository` | Persistence and external data access |
| `com.redhat.lightwell.model` | Domain entities, DTOs, enums |
| `com.redhat.lightwell.api` | Generated API interfaces (OpenAPI Generator, when used) |

## 🎯 Sample feature: Greetings API

The Hello World greeting demonstrates the layered flow:

```
GET /api/greetings?name=Alice
        │
        ▼
GreetingController          ← HTTP mapping, delegates to service
        │
        ▼
GreetingService             ← Builds "Hello, Alice!" message
        │
        ▼
GreetingResponse            ← JSON DTO returned to client
```

| Class | Layer | Description |
|-------|-------|-------------|
| `GreetingController` | Controller | Exposes `GET /api/greetings` with optional `name` query param |
| `GreetingService` | Service | Resolves name (defaults to `World`) and formats message |
| `GreetingResponse` | Model | DTO with `message` field serialized as JSON |

API details: [API.md](./API.md) and [openapi/openapi.yaml](../openapi/openapi.yaml)

## 📐 Design principles

1. **Thin controllers** — Handle HTTP concerns only (mapping, status codes, validation triggers).
2. **Services own business logic** — Domain workflows live in the service layer.
3. **Constructor injection** — Explicit dependencies via constructor; enable easier testing.
4. **Configuration externalized** — Use `application.properties` and environment-specific profiles.
5. **Fail fast** — Validate inputs at boundaries; return clear HTTP error responses.
6. **API-first contract** — Define [openapi/openapi.yaml](../openapi/openapi.yaml) before
   implementation; generate or align code with OpenAPI Generator.

## ⚙️ Configuration

- Default config: `src/main/resources/application.properties`
- Profile-specific overrides: `application-<profile>.properties` (e.g. `application-dev.properties`)
- Secrets and environment-specific values: environment variables—not committed to Git

## 🌍 API conventions (REST)

- Base path prefix: `/api/`
- Standard HTTP methods and status codes
- JSON request/response bodies
- Public endpoints defined in [openapi/openapi.yaml](../openapi/openapi.yaml) and documented in
  [API.md](./API.md)

## 🔌 Integration boundaries

| Integration | Status | Notes |
|-------------|--------|-------|
| Database | Not used | Repository layer reserved for future persistence |
| External HTTP APIs | Not defined | Use `RestTemplate` or `WebClient` when required |
| Messaging | Not defined | — |

## 🚫 Non-goals (initial scope)

- Microservices or multi-module Maven reactor
- Reactive stack (WebFlux)
- Complex domain-driven design beyond simple POC needs

## 🔗 Related documents

- [OpenAPI contract](../openapi/openapi.yaml)
- [API reference](./API.md)
- [Testing practices](./TESTING.md)
- [Build and run](./BUILD-AND-RUN.md)
- [Documentation practices](./DOCUMENTATION.md)
