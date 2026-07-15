# 📚 Documentation practices

Rules and conventions for maintaining documentation in the Spring Boot LW POC repository.

Mandatory code and doc conventions: [CONVENTIONS.md](./CONVENTIONS.md).

## 📌 Principles

1. **Single source of truth** — Each topic has one primary document; others link to it rather than
   duplicating content.
2. **Audience-aware** — Human docs in Markdown; agent operational rules in `AGENTS.md`; discovery
   index in `llms.txt`.
3. **Keep docs close to code** — When adding features, update the relevant doc in the same change
   set.
4. **Prefer links over copies** — Reference official Spring/Maven docs instead of pasting large
   excerpts.

## 📂 Document roles

| File | Audience | Content |
|------|----------|---------|
| [README.md](../README.md) | Human developers | Project overview, component index, quick start |
| [AGENTS.md](../AGENTS.md) | AI coding agents | Commands, conventions, boundaries (concise map) |
| [llms.txt](../llms.txt) | LLMs / agents | Structured index with links to key docs |
| [docs/ARCHITECTURE.md](./ARCHITECTURE.md) | All | Design, layers, package layout |
| [docs/API.md](./API.md) | All | REST API workflow, samples, links to OpenAPI contract |
| [openapi/openapi.yaml](../openapi/openapi.yaml) | All | Machine-readable API contract (source of truth) |
| [docs/TESTING.md](./TESTING.md) | All | Test strategy and commands |
| [docs/BUILD-AND-RUN.md](./BUILD-AND-RUN.md) | All | Local build and execution |
| [docs/DOCUMENTATION.md](./DOCUMENTATION.md) | All | This file — meta rules |
| [docs/CONVENTIONS.md](./CONVENTIONS.md) | All | Code and documentation conventions for any change |
| [.mavenrc.example](../.mavenrc.example) | Developers | Optional local Maven/TLS setup template |

## ✍️ Formatting rules

- Use **Markdown** (`.md`) for all documentation files under `docs/`.
- **Uppercase file names** in `docs/` (for example `API.md`, `CONVENTIONS.md`).
- Use ATX headings (`#`, `##`, `###`); one `#` title per file; **every heading starts with an
  emoji** (see [CONVENTIONS.md](./CONVENTIONS.md)).
- Use fenced code blocks with language tags (`bash`, `java`, `xml`).
- Use tables for structured comparisons (stack versions, file maps).
- **Maximum line length: 120 characters**; wrap long prose for readability in Git diffs.

## 📝 Writing style

- Write in **clear, direct English**
- Use present tense for descriptions ("The service validates…")
- Use imperative for instructions ("Run `mvn -s settings.xml test`")
- Define acronyms on first use in each document
- Avoid time-sensitive wording ("currently", "recently") unless dated

## 🔄 When to update documentation

Update docs when you:

- Add or remove a Maven dependency with architectural impact
- Change public API paths or behavior (**update `openapi/openapi.yaml` first**)
- Introduce a new layer, module, or integration
- Change build, test, or run commands
- Add environment variables or configuration properties

## 💬 Code comments vs documentation

| Use code comments for | Use Markdown docs for |
|-----------------------|----------------------|
| Non-obvious algorithm or workaround | Architecture and design decisions |
| TODO with ticket reference | How to build, test, and run |
| API contract nuances in public JavaDoc | Repository-wide conventions |

Do not use comments to replace architecture or setup documentation.

## 📖 JavaDoc

Follow [CONVENTIONS.md](./CONVENTIONS.md): Javadoc on every non-private class and method; document
parameters, return values, and exceptions where applicable.

## 🔢 Version references

When citing framework versions in docs, use the project baseline unless documenting an intentional
upgrade:

- Spring Boot: **2.7.18**
- Java: **17**

Link to version-specific official docs where possible (see [llms.txt](../llms.txt) External
references).

## 🤖 Agent-specific files

- **AGENTS.md** stays under ~150 lines; link to `docs/` for detail
- **llms.txt** follows [llmstxt.org](https://llmstxt.org/) format: title, summary, sections with
  linked resources
- **`.cursor/rules/*.mdc`** — Short, scoped Cursor rules; must not contradict AGENTS.md

## ✅ Review checklist

Before merging documentation changes:

- [ ] Links resolve correctly (relative paths)
- [ ] Commands were verified locally (when applicable)
- [ ] No duplicated sections across README and AGENTS.md
- [ ] llms.txt updated if new doc files were added
- [ ] Headings use emojis; doc file names in `docs/` are uppercase
- [ ] Changes align with [CONVENTIONS.md](./CONVENTIONS.md)

## 🔗 Related documents

- [CONVENTIONS.md](./CONVENTIONS.md)
- [README.md](../README.md)
- [AGENTS.md](../AGENTS.md)
