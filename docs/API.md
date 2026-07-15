# 🌐 REST API

Human-readable guide for the **API-first** REST contract of the Spring Boot LW POC.

The machine-readable contract lives in **[openapi/openapi.yaml](../openapi/openapi.yaml)**. That file
must be updated **before** any controller, DTO, or service change. Implementation is generated or
aligned from the contract using the OpenAPI Generator Maven plugin (see [🔧 Code generation](#-code-generation)).

## 📜 Contract source of truth

| Artifact | Path | Role |
|----------|------|------|
| OpenAPI specification | [openapi/openapi.yaml](../openapi/openapi.yaml) | Authoritative API contract (paths, schemas, examples) |
| Swagger UI (runtime) | `http://localhost:8080/swagger-ui.html` | Interactive docs and API testing |
| OpenAPI URL (runtime) | `http://localhost:8080/openapi/openapi.yaml` | Contract served when app is running |
| This document | `docs/API.md` | Workflow, usage samples, and links for humans and agents |
| Generated / aligned code | `com.redhat.lightwell.controller`, `.model`, `.service` | Must match the OpenAPI contract |

Base URL (local): `http://localhost:8080`

## 🖥️ Online documentation (Swagger UI)

When the application is running, consumers can browse and **try** the API from a browser using
**Swagger UI**, which loads the same contract as [openapi/openapi.yaml](../openapi/openapi.yaml).

| Resource | URL (local) | Description |
|----------|-------------|-------------|
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interactive API explorer and "Try it out" |
| OpenAPI contract | [http://localhost:8080/openapi/openapi.yaml](http://localhost:8080/openapi/openapi.yaml) | Raw YAML served at runtime |

Start the app, then open Swagger UI:

```bash
mvn -s settings.xml spring-boot:run
# Browser: http://localhost:8080/swagger-ui.html
```

Swagger UI is powered by [springdoc-openapi](https://springdoc.org/) and loads the **authored contract**
from `/openapi/openapi.yaml` (`springdoc.swagger-ui.url`) so consumers browse and test the API-first
spec—not a runtime-generated snapshot from controller annotations.

## 🔄 API-first workflow

Every new or changed public HTTP endpoint follows this order:

```
1. Edit openapi/openapi.yaml     ← describe paths, parameters, responses, examples
2. Review docs/API.md              ← add human notes if the change needs extra context
3. Run OpenAPI Generator           ← scaffold new code or refresh stubs/interfaces
4. Implement business logic        ← service layer only; keep controllers thin
5. Run tests + curl samples        ← confirm behaviour matches the contract
```

**Do not** add a public route to Java code without a matching entry in `openapi/openapi.yaml`.

### 🆕 First-time endpoint

1. Add the operation, parameters, response schema, and at least one `examples` block in
   `openapi/openapi.yaml`.
2. Run code generation (see [🔧 Code generation](#-code-generation)).
3. Wire the generated controller delegate to a new or existing service method.
4. Add or update tests and the curl samples below.

### ♻️ Refactoring an existing endpoint

1. Change `openapi/openapi.yaml` first (path, parameter, schema, or response).
2. Re-run code generation to refresh generated interfaces or stubs.
3. Adjust the existing controller, DTO, and service so runtime behaviour matches the updated
   contract.
4. Update tests and samples; run `mvn -s settings.xml clean verify`.

Architecture context: [ARCHITECTURE.md](./ARCHITECTURE.md#-api-first-design).

## 🔧 Code generation

The project uses the **OpenAPI Generator Maven plugin** to produce Spring-compatible API artifacts
from `openapi/openapi.yaml`. The plugin generates controller interfaces and model classes; business
logic remains in hand-written service classes.

Generate or refresh API stubs (from the repository root):

```bash
mvn -s settings.xml openapi-generator:generate
```

Generated output is configured in `pom.xml` (package `com.redhat.lightwell`, Spring Boot generator).
After generation, merge or adapt existing classes so services and tests continue to pass.

## 📋 Implemented operations

The table lists operations **defined in the contract** and **implemented** in this repository.
Details, schemas, and machine-readable examples are in [openapi/openapi.yaml](../openapi/openapi.yaml).

| Method | Path | operationId | Summary | Java controller |
|--------|------|-------------|---------|-----------------|
| GET | `/api/greetings` | `getGreeting` | Return a personalized greeting | `GreetingController#greet` |

## 👋 Greetings

### 🔹 `GET /api/greetings` — `getGreeting`

Returns a personalized greeting message for an optional user name.

| Parameter | In | Type | Required | Default | Description |
|-----------|-----|------|----------|---------|-------------|
| `name` | query | string | No | `World` (logical) | User name to greet. Omitted, blank, or whitespace-only values use `World`. |

**Responses**

| Status | Description | Body |
|--------|-------------|------|
| 200 | Greeting generated successfully | `GreetingResponse` JSON |

**Schema: `GreetingResponse`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `message` | string | Yes | Formatted greeting text (e.g. `Hello, Alice!`) |

### 📎 Usage samples

Confirm the contract after starting the application (`mvn -s settings.xml spring-boot:run`).

**Sample 1 — default greeting (no `name` parameter)**

Request:

```bash
curl -s http://localhost:8080/api/greetings
```

Expected response (`200 OK`):

```json
{"message":"Hello, World!"}
```

**Sample 2 — personalized greeting**

Request:

```bash
curl -s "http://localhost:8080/api/greetings?name=Alice"
```

Expected response (`200 OK`):

```json
{"message":"Hello, Alice!"}
```

**Sample 3 — blank name falls back to default**

Request:

```bash
curl -s "http://localhost:8080/api/greetings?name=%20%20"
```

Expected response (`200 OK`):

```json
{"message":"Hello, World!"}
```

### 🏗️ Implementation map

| Layer | Class | Role |
|-------|-------|------|
| Contract | [openapi/openapi.yaml](../openapi/openapi.yaml) | Defines `getGreeting` operation and `GreetingResponse` schema |
| Controller | `com.redhat.lightwell.controller.GreetingController` | HTTP mapping; delegates to service |
| Service | `com.redhat.lightwell.service.GreetingService` | Builds greeting message (business logic) |
| Model | `com.redhat.lightwell.model.GreetingResponse` | JSON response DTO |

## ✅ Contract checklist

Before merging an API change:

- [ ] `openapi/openapi.yaml` updated with operation, schemas, and examples
- [ ] `docs/API.md` updated (this file) if workflow or samples change
- [ ] OpenAPI Generator run when interfaces or models are affected
- [ ] Service and controller behaviour matches the spec
- [ ] curl samples above (or new ones) verified locally
- [ ] `mvn -s settings.xml clean verify` passes

## 🔗 Related documents

- [OpenAPI contract](../openapi/openapi.yaml)
- [Architecture](./ARCHITECTURE.md)
- [Conventions](./CONVENTIONS.md)
- [Build and run](./BUILD-AND-RUN.md)
- [README](../README.md)
