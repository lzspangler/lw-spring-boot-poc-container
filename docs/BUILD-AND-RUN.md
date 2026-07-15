# 🚀 Build, package, and run locally

Instructions for building and running the Spring Boot LW POC on a developer machine.

## 📋 Prerequisites

| Requirement | Version | Verification |
|-------------|---------|--------------|
| JDK | 17 | `java -version` |
| Apache Maven | 3.6.3+ (3.8+ recommended) | `mvn -version` |
| Git | Any recent | `git --version` |

Ensure `JAVA_HOME` points to a JDK 17 installation and appears first in your `PATH`.

### 🛠️ Optional

- IDE with Java 17 and Maven support (IntelliJ IDEA, Eclipse, VS Code with Extension Pack for Java)
- `curl` or HTTP client for testing REST endpoints after startup

## ⚙️ Maven settings

This project ships a **`settings.xml`** at the repository root. It configures Maven profiles and
Red Hat Lightwell Network repositories used to resolve dependencies for this POC. There is also
an example of integration by a local instance of Nexus in the **`settings-nexus.xml` file at the
repository root.

Always pass it explicitly when running Maven from the repository root:

```bash
mvn -s settings.xml <goal>
```

Equivalent long form: `mvn --settings settings.xml <goal>`.

Do not commit real credentials in `settings.xml`. Use environment-specific secrets or a local
override file ignored by version control when authentication is required. There are two variables
defined to set up the credentials to download dependencies from the Red Hat Lightwell Network:

- `LW_USERNAME` - Pattern `XXXXXXX|service-account-name`.
- `LW_PASSWORD` - Token generated for the service account.

For detailed information about how to create those credentials, review the [official documentation
of Red Hat Lightwell](https://docs.redhat.com/en/documentation/red_hat_lightwell_network/current/get_started-create_a_red_hat_lightwell_network_service_account).

Example:

```bash
export LW_USERNAME='XXXXXXX|lightwell'
export LW_PASSWORD='your-service-account-token'
mvn -s settings.xml clean verify
```

### 🗂️ Active Maven profiles

`settings.xml` activates both Lightwell profiles by default:

| Profile | Repository | Purpose |
|---------|------------|---------|
| `lightwell-validated` | `.../java/validated` | Validated dependency stream |
| `lightwell-remediated` | `.../java/remediated` | Remediated dependency stream (e.g. `*.rhlw-*` versions in `pom.xml`) |

Disable a profile for a single run with `-P !lightwell-remediated` if you only need validated
artifacts.

## 🔨 Project build (Maven)

All commands run from the **repository root** (directory containing `pom.xml` and `settings.xml`).
Each example includes `-s settings.xml`.

### 📦 Compile

```bash
mvn -s settings.xml compile
```

Compiles main sources to `target/classes/`.

### 🧪 Run tests

```bash
mvn -s settings.xml test
```

Runs 7 tests (service unit tests, controller slice tests, context load). See [TESTING.md](./TESTING.md).

### ✅ Full verify (recommended)

```bash
mvn -s settings.xml clean verify
```

Cleans previous artifacts, compiles, runs tests, and packages the application.

### 📦 Package executable JAR

```bash
mvn -s settings.xml clean package
```

Produces an executable "fat" JAR via `spring-boot-maven-plugin`:

```
target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar
```

Skip tests during packaging (not recommended for release):

```bash
mvn -s settings.xml clean package -DskipTests
```

## ▶️ Run locally

### 🥇 Option 1 — Maven plugin (development)

```bash
mvn -s settings.xml spring-boot:run
```

Starts the embedded web server on port **8080**.

With a Spring profile:

```bash
mvn -s settings.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🥈 Option 2 — Executable JAR

```bash
java -jar target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar
```

With profile:

```bash
java -jar -Dspring.profiles.active=dev target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar
```

### 🥉 Option 3 — IDE

Run `com.redhat.lightwell.Application` from your IDE's run configuration. Use JDK 17.

## 📄 OpenAPI code generation

After changing [openapi/openapi.yaml](../openapi/openapi.yaml), regenerate API interfaces and models:

```bash
mvn -s settings.xml openapi-generator:generate
```

Output directory: `target/generated-sources/openapi`. See [API.md](./API.md) for the API-first
workflow.

## 🔧 Configuration

| File | Purpose |
|------|---------|
| `settings.xml` | Lightwell Maven profiles; `${env.LW_USERNAME}` / `${env.LW_PASSWORD}` |
| `.mavenrc.example` | Template for optional `~/.mavenrc` (TLS trust store, `JAVA_HOME`) |
| `src/main/resources/application.properties` | Default application settings (`server.port=8080`) |

Override at runtime with environment variables or JVM system properties:

```bash
export SERVER_PORT=9090
mvn -s settings.xml spring-boot:run
```

Do not commit secrets (passwords, API keys) to these files.

## ✔️ Verify the application is running

```bash
# Default greeting
curl -s http://localhost:8080/api/greetings

# Personalized greeting
curl -s "http://localhost:8080/api/greetings?name=Alice"
```

Expected responses:

```json
{"message":"Hello, World!"}
{"message":"Hello, Alice!"}
```

Default base URL: `http://localhost:8080`

See [API.md](./API.md) for full API reference.

### 🖥️ Swagger UI (interactive OpenAPI)

With the application running:

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI YAML | http://localhost:8080/openapi/openapi.yaml |

Open Swagger UI in a browser to browse the contract and use **Try it out** on each operation.

## 🔍 Troubleshooting

| Issue | Likely cause | Action |
|-------|--------------|--------|
| `401 Unauthorized` (Lightwell) | Missing or expired `LW_USERNAME` / `LW_PASSWORD` | Export credentials; verify service account token |
| `PKIX path building failed` | JVM does not trust registry CA | Import CA into trust store; set `MAVEN_OPTS` (see TLS section) |
| OpenAPI Generator `Read timed out` | Maven Central plugin download timeout | Retry; check network; plugin resolves from Central, not Lightwell |
| `invalid target release: 17` | Wrong JDK | Set `JAVA_HOME` to JDK 17 |
| Port 8080 already in use | Another process | Change `server.port` or stop conflicting service |
| Tests fail in CI but pass locally | Profile or env difference | Align `spring.profiles.active` and env vars |
| `mvn` not found | Maven not installed | Install Maven and add to `PATH` |

## 🐳 Docker (future)

Container build instructions may be added later. Until then, use local Maven workflow above.

## 🔗 Related documents

- [API reference](./API.md)
- [Architecture](./ARCHITECTURE.md)
- [Testing](./TESTING.md)
- [Agent guide (AGENTS.md)](../AGENTS.md)
