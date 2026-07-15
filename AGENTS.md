# 🤖 Agent guide — Spring Boot LW POC

This repository is a **simple Spring Boot application** (proof of concept). Agents should use this file as the operational map; detailed rules live in linked docs under `docs/`.

## 🧱 Stack (fixed)

| Item | Value |
|------|-------|
| Framework | Spring Boot **2.7.18** |
| Language | Java **17** |
| Build | **Maven** (`pom.xml`, groupId `com.redhat.lightwell`) |
| Packaging | Executable JAR (`spring-boot-maven-plugin`) |

Do not upgrade Spring Boot or Java without an explicit request.

## 🔐 Environment (required)

Maven resolves dependencies from Red Hat Lightwell repositories. Export credentials before any
`mvn` command (see [docs/BUILD-AND-RUN.md](docs/BUILD-AND-RUN.md)):

```bash
export LW_USERNAME='XXXXXXX|your-service-account'
export LW_PASSWORD='your-token'
```

Never commit credentials. `settings.xml` references `${env.LW_USERNAME}` and `${env.LW_PASSWORD}`.

## 📁 Project structure

GroupId: `com.redhat.lightwell`. Base package: `com.redhat.lightwell`

```
src/main/java/com/redhat/lightwell/
├── Application.java
├── controller/GreetingController.java
├── service/GreetingService.java
└── model/GreetingResponse.java
src/main/resources/application.properties
src/test/java/com/redhat/lightwell/   # Mirror main package structure
pom.xml
settings.xml                       # Pass with -s settings.xml on every mvn command
openapi/openapi.yaml               # API contract (edit before implementation)
target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar
```

Sample API: `GET /api/greetings?name=<user>` — contract in [openapi/openapi.yaml](openapi/openapi.yaml),
guide in [docs/API.md](docs/API.md).

## ⚡ Commands

Run from the repository root once `pom.xml` exists. Pass the project **`settings.xml`** with
`-s settings.xml` on every Maven invocation (see [docs/BUILD-AND-RUN.md](docs/BUILD-AND-RUN.md)).

```bash
# Build (compile + unit tests)
mvn -s settings.xml clean verify

# Package executable JAR (skip tests)
mvn -s settings.xml clean package -DskipTests

# Run locally
mvn -s settings.xml spring-boot:run

# Run packaged JAR
java -jar target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar

# Generate API interfaces/models from openapi/openapi.yaml (after contract changes)
mvn -s settings.xml openapi-generator:generate
```

See [docs/BUILD-AND-RUN.md](docs/BUILD-AND-RUN.md) for prerequisites, profiles, and troubleshooting.

## 🗺️ Documentation map

| Topic | File |
|-------|------|
| Architecture | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| REST API | [docs/API.md](docs/API.md) |
| OpenAPI contract | [openapi/openapi.yaml](openapi/openapi.yaml) |
| Testing | [docs/TESTING.md](docs/TESTING.md) |
| Documentation rules | [docs/DOCUMENTATION.md](docs/DOCUMENTATION.md) |
| Conventions | [docs/CONVENTIONS.md](docs/CONVENTIONS.md) |
| Build & run | [docs/BUILD-AND-RUN.md](docs/BUILD-AND-RUN.md) |
| Human overview | [README.md](README.md) |
| LLM index | [llms.txt](llms.txt) |

## 💻 Coding conventions

Full rules (formatting, Javadoc, API-first): [docs/CONVENTIONS.md](docs/CONVENTIONS.md).

- **API-first:** edit [openapi/openapi.yaml](openapi/openapi.yaml) before controllers or DTOs; then
  run `mvn -s settings.xml openapi-generator:generate` when interfaces/models are affected.
- Use **constructor injection** for dependencies; avoid field injection.
- Keep controllers thin: validate input, delegate to services, return responses.
- Put business logic in services, not controllers or repositories.
- Use meaningful package names aligned with layers (`controller`, `service`, `repository`, `model`).
- Prefer `application.properties` for configuration unless YAML is explicitly chosen project-wide.
- Follow existing naming in the repo before introducing new patterns.

### 📎 Examples

```java
// ✅ GOOD — constructor injection, thin controller
@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable Long id) {
        return itemService.findById(id);
    }
}

// ❌ BAD — field injection, business logic in controller
@RestController
public class ItemController {
    @Autowired
    private ItemRepository repo;

    @GetMapping("/{id}")
    public Item get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(); // logic belongs in service
    }
}
```

## 🧪 Testing

- Run the full suite with `mvn -s settings.xml test` or `mvn -s settings.xml verify`.
- Follow [docs/TESTING.md](docs/TESTING.md) for unit vs integration tests, naming, and coverage expectations.

## 🚧 Boundaries

- **Do not** change Spring Boot version, Java version, or build tool without explicit approval.
- **Do not** add frameworks (reactive stack, extra ORMs, etc.) unless documented in architecture first.
- **Do not** commit secrets, credentials, or local-only config with real values.
- **Do not** create commits or pull requests unless the user asks.
- Search the codebase for existing helpers before adding new utility classes.

## 🔒 Security

- Never hard-code passwords, API keys, or tokens; use environment variables or Spring `@ConfigurationProperties`.
- Validate and sanitize external input at controller boundaries.
- Set `LW_USERNAME` and `LW_PASSWORD` for Lightwell Maven repository access; optional TLS trust via
  `MAVEN_OPTS` (see [docs/BUILD-AND-RUN.md](docs/BUILD-AND-RUN.md)).
