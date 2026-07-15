# 📋 Project conventions

Mandatory conventions for **any change** in the Spring Boot LW POC repository — code, APIs, and
documentation.

For build commands, layer rules, and agent boundaries, see [AGENTS.md](../AGENTS.md). For how to
maintain docs, see [DOCUMENTATION.md](./DOCUMENTATION.md).

## 💻 Code

### ☕ Java syntax and formatting

- Follow **standard Java syntax and formatting** widely used in the Java community (Google Java
  Style Guide and OpenJDK conventions as reference).
- Use **4 spaces** for indentation; no tabs.
- Place one top-level type per file; file name matches the public type.
- Organize imports; avoid wildcard imports except for static test utilities when appropriate.
- **Maximum line length: 120 characters.** Wrap long signatures, strings, and call chains across
  lines; do not disable the limit in project-wide formatter settings without team approval.
- All files (e.g: .java, .xml, .yaml, .json) use spaces instead of tabs.

### 📖 Javadoc

- Every **non-private class** and **non-private method** must have a Javadoc block that describes
  its purpose.
- Document parameters (`@param`), return values (`@return`), and checked exceptions (`@throws`)
  where applicable.
- Keep summaries concise; add detail only when behavior is not obvious from the signature.
- Private members do not require Javadoc unless the logic is unusually complex (use a brief inline
  comment instead).

Example:

```java
/**
 * Builds a greeting message for the given user name.
 *
 * @param name display name; blank values are treated as the default
 * @return greeting text ready for the HTTP response
 */
public String buildGreeting(String name) {
    // ...
}
```

### 🌐 API-first contract

**API-first** is the primary structure for this project:

1. **Define the contract first** — paths, parameters, request/response shapes, status codes,
   examples, and error behaviour in **[openapi/openapi.yaml](../openapi/openapi.yaml)** before
   any controller, DTO, or service implementation.
2. **Document for humans** — summarise workflow and usage samples in [API.md](./API.md); keep
   `openapi/openapi.yaml` as the machine-readable source of truth.
3. **Generate or align code** — run the OpenAPI Generator Maven plugin to scaffold new endpoints
   or refresh generated interfaces when the contract changes (see [API.md](./API.md) and
   [ARCHITECTURE.md](./ARCHITECTURE.md)).
4. **Implement business logic** — hand-written services fulfil the contract; controllers stay thin.
5. **Do not ship undocumented public HTTP endpoints** — every exposed route must appear in
   `openapi/openapi.yaml` and [API.md](./API.md).

Layered design (controller → service → repository → model) remains mandatory; see
[ARCHITECTURE.md](./ARCHITECTURE.md).

### 🏷️ API and method documentation annotations

- Every **public API surface** (REST endpoints, request/response types, and service methods
  consumed across layers) must carry **documentation that describes it**:
  - **Java:** Javadoc on classes and non-private methods (see above).
  - **HTTP:** describe each endpoint in [openapi/openapi.yaml](../openapi/openapi.yaml) with
    parameters, responses, and examples; summarise workflow and samples in [API.md](./API.md).
    Annotate controllers and DTOs with OpenAPI annotations (for example `@Tag`, `@Operation`,
    `@Parameter`, `@Schema`) when present so runtime docs stay aligned with the contract.
- Method names, path mappings, and DTO fields should be self-explanatory; documentation fills in
  semantics, defaults, validation rules, and error cases.

### 🔗 Related code conventions

Project-specific patterns (constructor injection, thin controllers, package layout) are summarized in
[AGENTS.md](../AGENTS.md). This file does not replace those rules; apply **both** sets of
conventions on every change.

## 📚 Documentation

### 📝 Format and file names

- Use **Markdown** (`.md`) for all documentation under `docs/` and for repository guides
  (`README.md`, `AGENTS.md`, etc.).
- **Document file names must be UPPERCASE** for the whole name (for example `CONVENTIONS.md`,
  `API.md`, `ARCHITECTURE.md`). Use underscores only when a name would otherwise be ambiguous.

### ✍️ Markdown style

- Follow **standard Markdown rules and best practices**: ATX headings (`#`, `##`), fenced code
  blocks with language tags, reference-style or inline links with meaningful anchor text, and
  tables for structured data.
- One `#` title per file; use a logical heading hierarchy (do not skip levels).
- **Maximum line length: 120 characters** in prose, tables, and list items where practical; wrap
  long lines to keep diffs readable.
- Write in clear, direct English; use present tense for descriptions and imperative mood for
  steps (see [DOCUMENTATION.md](./DOCUMENTATION.md)).

### 😀 Titles and emojis

- **Every heading** in a documentation file (`#`, `##`, `###`, and below) must **start with an
  emoji** that hints at the section content.
- Use the emoji **once**, at the beginning of the heading text (for example `## 🚀 Quick start`).
- Pick emojis that are widely recognized and consistent within a document; avoid decorative chains
  of multiple emojis in one title.
- The file title (`#`) follows the same rule (for example `# 📋 Project conventions`).

### 🔄 When conventions apply

Apply these documentation conventions to:

- New or updated files under `docs/`
- Changes to `README.md`, `AGENTS.md`, and `llms.txt` when those files are edited for the same
  feature or fix

When adding a new doc file, update [llms.txt](../llms.txt) and the document roles table in
[DOCUMENTATION.md](./DOCUMENTATION.md).

## ✅ Checklist (any change)

- [ ] Code follows Java community formatting; lines ≤ 120 characters
- [ ] Non-private classes and methods have Javadoc
- [ ] Public API changes are reflected in [openapi/openapi.yaml](../openapi/openapi.yaml) and
      [API.md](./API.md) (API-first)
- [ ] HTTP endpoints and cross-layer APIs are documented (Javadoc + API reference / OpenAPI when
      enabled)
- [ ] Doc edits use Markdown, uppercase filenames in `docs/`, lines ≤ 120 characters
- [ ] All headings in touched doc files start with an emoji

## 🔗 Related documents

- [AGENTS.md](../AGENTS.md)
- [DOCUMENTATION.md](./DOCUMENTATION.md)
- [API.md](./API.md)
- [openapi/openapi.yaml](../openapi/openapi.yaml)
- [ARCHITECTURE.md](./ARCHITECTURE.md)
