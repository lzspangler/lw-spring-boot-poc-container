# Red Hat Lightwell Network — FSI Demo Slide Outline

> Securing Legacy Java in Financial Services Without the Upgrade Risk

---

## Slide 1: Title

**Securing Spring Boot in Financial Services with Red Hat Lightwell**

Remediate CVEs. Stay on your current version. Ship with confidence.

---

## Slide 2: The Problem — Legacy Java in Financial Services

- Financial institutions run mission-critical systems on Spring Boot 2.7.x and Java 17
- Spring Boot 2.7.x reached end of OSS support in November 2023
- These applications process payments, manage accounts, handle regulatory documents, and run credit checks
- Upgrading to Spring Boot 3.x requires:
  - Jakarta EE 9+ migration (`javax.*` → `jakarta.*`)
  - Java 17+ baseline (already met, but Spring Boot 3.2+ recommends 21)
  - Extensive regression testing against regulatory and compliance frameworks
  - Re-certification with auditors (SOC 2, PCI-DSS, SOX)
- **Estimated upgrade cost: 6–18 months of engineering effort for a typical FSI platform**

---

## Slide 3: The Risk — CVEs Are Accumulating

- Spring Boot 2.7.18 and its transitive dependencies carry **known CVEs** that scanners flag daily
- Examples from this demo application:
  - **SnakeYAML 1.33** — CVE-2022-1471: arbitrary code execution via YAML deserialization
  - **Apache HttpClient 4.5.12** — CVE-2023-45648: request smuggling
  - **Woodstox 6.0.3** — CVE-2022-40152: XML external entity (XXE) attacks
  - **json-smart 2.4.8** — CVE-2023-1370: stack overflow via crafted JSON
  - **Spring Security 5.7.11** — CVE-2025-22235: authorization bypass
  - **Logback 1.2.11** — CVE-2023-6378: denial of service
  - **H2 Database 2.1.214** — CVE-2022-45868: remote code execution
- Compliance teams, auditors, and regulators see these in every scan
- **Every unfixed CVE is a finding in your next audit**

---

## Slide 4: The Dilemma — Upgrade vs. Stay Vulnerable

| Option | Risk | Cost | Timeline |
|--------|------|------|----------|
| **Upgrade to Spring Boot 3.x** | Regression risk, re-certification | High | 6–18 months |
| **Stay on 2.7.x unpatched** | Known CVEs, audit findings | Low (short term) | Ongoing liability |
| **Red Hat Lightwell** | Minimal — same base version | Subscription | Days, not months |

---

## Slide 5: The Lightwell Solution — Remediated Dependencies

- **Red Hat Lightwell Network** provides remediated Java dependencies
- Security fixes are **backported** to your current version — no upgrade required
- Version scheme: `2.7.18.rhlw-00004` — same base, patched artifact
- Your application code doesn't change. Your dependency versions don't change. Only the artifacts are patched.
- Available through standard Maven repositories with authenticated access
- Two repository types:
  - **Validated** — upstream artifacts verified and certified by Red Hat
  - **Remediated** — actively patched with CVE fixes backported to the original version

---

## Slide 6: How It Works — No Code Changes

```xml
<!-- BEFORE: Vulnerable -->
<version>${spring.boot.version}</version>         <!-- 2.7.18 -->
<version>1.33</version>                           <!-- snakeyaml -->
<version>4.5.12</version>                         <!-- httpclient -->

<!-- AFTER: Remediated — same base version -->
<version>${spring.boot.version}.rhlw-00004</version>  <!-- 2.7.18.rhlw-00004 -->
<version>1.33.0.rhlw-00001</version>                  <!-- snakeyaml -->
<version>4.5.12.rhlw-00001</version>                  <!-- httpclient -->
```

- One script to apply: `./apply-rhlw-version.sh`
- One script to revert: `./revert-rhlw-version.sh`
- All 80 tests pass on both versions
- Binary-compatible — no recompilation of your source code needed

---

## Slide 7: Live Demo — Before (Vulnerable)

> **[Switch to VSCode — show pom.xml in non-remediated state]**

- Open `pom.xml` — all dependencies at upstream versions
- Run **Red Hat Dependency Analytics** scan
- Show CVE count: **X direct vulnerabilities** across Spring Boot, SnakeYAML, HttpClient, Woodstox, json-smart, Logback, H2, Spring Security, commons-io, commons-fileupload
- Highlight critical/high severity CVEs relevant to FSI:
  - Remote code execution (SnakeYAML, H2)
  - Authorization bypass (Spring Security)
  - Request smuggling (HttpClient)

---

## Slide 8: Live Demo — After (Remediated)

> **[Run `./apply-rhlw-version.sh` in terminal]**

- Show the pom.xml diff — only version suffixes changed
- Re-run **Red Hat Dependency Analytics** scan
- Show CVE count: **significantly reduced**
- Same application. Same Spring Boot 2.7.18 base. Same Java 17. Same 80 passing tests.
- The application still does everything it did before:
  - Customer management and account operations
  - Fund transfers and transaction history
  - Document upload and storage (commons-io)
  - Credit bureau integration (Apache HttpClient)
  - XML statement import (Woodstox)
  - Admin JSON queries (json-smart)
  - YAML config import (SnakeYAML)

---

## Slide 9: The Application — Real FSI Workloads

This is not a "hello world" — it's a retail banking platform with:

| Feature | Vulnerable Dependency Exercised |
|---------|-------------------------------|
| Customer & account management | Spring Boot, Spring Security, Spring Data JPA |
| Fund transfers & transactions | Spring Boot (validation, transaction management) |
| Document upload & storage | Apache Commons IO, Commons FileUpload |
| Credit bureau checks | Apache HttpClient 4.x |
| XML bank statement import | Woodstox (StAX XML parser) |
| Admin JSON data queries | json-smart |
| YAML configuration import | SnakeYAML |
| Audit logging | Logback |
| In-memory database | H2 |
| Role-based access control | Spring Security (RBAC: ADMIN, USER) |

Each feature exercises the exact library that Lightwell remediates — proving the patches work in real FSI workflows.

---

## Slide 10: Business Value

| Metric | Without Lightwell | With Lightwell |
|--------|-------------------|----------------|
| **CVE remediation time** | 6–18 months (full upgrade) | Days |
| **Code changes required** | Thousands of lines (javax→jakarta) | Zero application code changes |
| **Test regression risk** | High | Minimal — same base version |
| **Audit findings** | Open CVEs flagged every cycle | Remediated, defensible |
| **Compliance posture** | Gaps documented, waivers needed | Clean scan results |
| **Ongoing patching** | Manual, per-CVE | Continuous via Lightwell subscription |

---

## Slide 11: How to Get Started

1. **Sign up** for a Red Hat Lightwell Network service account
2. **Configure Maven** — add `settings.xml` with Lightwell repository credentials
3. **Apply remediated versions** — run `./apply-rhlw-version.sh` or update pom.xml manually
4. **Build and test** — `mvn -s settings.xml clean verify`
5. **Scan** — re-run dependency analysis to confirm CVE reduction
6. **Ship** — deploy with confidence, same application, fewer vulnerabilities

---

## Slide 12: Q&A

**Resources:**

- [Red Hat Lightwell Network Documentation](https://docs.redhat.com/en/documentation/red_hat_lightwell_network/)
- [Red Hat Dependency Analytics — VSCode Extension](https://marketplace.visualstudio.com/items?itemName=redhat.fabric8-analytics)
- Demo repository: this project (`spring-boot-lw-poc`)

---

## Appendix A: Dependencies Remediated in This Demo

| Dependency | Upstream Version | Lightwell Version | Key CVEs Addressed |
|------------|-----------------|--------------------|--------------------|
| Spring Boot (all starters) | 2.7.18 | 2.7.18.rhlw-00004 | Multiple Spring Framework CVEs |
| Spring Security Core | 5.7.11 | 5.7.11.rhlw-00006 | CVE-2025-22235 (authz bypass) |
| SnakeYAML | 1.33 | 1.33.0.rhlw-00001 | CVE-2022-1471 (RCE via deserialization) |
| Logback Classic | 1.2.11 | 1.2.11.rhlw-00001 | CVE-2023-6378 (DoS) |
| Apache Commons FileUpload | 1.4 | 1.4.0.rhlw-00001 | CVE-2023-24998 (DoS) |
| Apache Commons IO | 2.11.0 | 2.11.0.rhlw-00001 | CVE-2024-47554 (path traversal) |
| Apache HttpClient | 4.5.12 | 4.5.12.rhlw-00001 | CVE-2023-45648 (request smuggling) |
| Woodstox Core | 6.0.3 | 6.0.3.rhlw-00001 | CVE-2022-40152 (XXE) |
| json-smart | 2.4.8 | 2.4.8.rhlw-00001 | CVE-2023-1370 (stack overflow) |
| H2 Database | 2.1.214 | 2.2.219.rhlw-00001 | CVE-2022-45868 (RCE) |

## Appendix B: FSI Regulatory Context

- **PCI-DSS 4.0** Requirement 6.3: Identify and manage security vulnerabilities in custom and third-party software
- **SOC 2** CC7.1: Monitor infrastructure and software for vulnerabilities
- **OSFI B-13** (Canada): Technology and cyber risk management — patch management expectations
- **FFIEC** (US): IT Examination Handbook — vulnerability management
- **DORA** (EU): Digital Operational Resilience Act — ICT third-party risk

Lightwell provides a defensible remediation posture without the operational risk of a major framework upgrade.
