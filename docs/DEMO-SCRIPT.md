# Red Hat Lightwell FSI Demo Script

> Step-by-step walkthrough for demonstrating Lightwell remediated dependency
> patching using VSCode and the Red Hat Dependency Analytics extension.

## Prerequisites

Before the demo:

1. **VSCode** with [Red Hat Dependency Analytics](https://marketplace.visualstudio.com/items?itemName=redhat.fabric8-analytics) extension installed
2. **Maven** installed with Java 17
3. **Lightwell credentials** exported:
   ```bash
   export LW_USERNAME='your-service-account'
   export LW_PASSWORD='your-token'
   ```
4. **Project cloned** and in non-remediated state:
   ```bash
   git clone <repo-url>
   cd spring-boot-lw-poc
   ./revert-rhlw-version.sh   # ensure clean "before" state
   ```
5. **Build verified** before demo:
   ```bash
   mvn -s settings.xml clean verify
   # Should show: Tests run: 80, Failures: 0, BUILD SUCCESS
   ```

---

## Part 1: Set the Scene (2 minutes)

### Talking points

> "We're looking at a Spring Boot 2.7.18 retail banking application — the kind
> of platform you'd find in production at any mid-size financial institution.
> It handles customer onboarding, account management, fund transfers,
> document storage, credit checks, and regulatory data imports.
>
> Spring Boot 2.7 is end of open-source support, but migrating to 3.x means
> a javax-to-jakarta namespace migration, extensive regression testing, and
> re-certification with auditors. For most FSI organizations, that's a
> 6-to-18-month project.
>
> In the meantime, CVEs keep accumulating. Let's see what that looks like."

---

## Part 2: Show the Vulnerable Application (5 minutes)

### Step 1: Open pom.xml in VSCode

1. Open the project in VSCode
2. Open `pom.xml` in the editor
3. Point out the key dependency versions:

> "Notice we're on Spring Boot 2.7.18 — the latest 2.7.x release. We've
> pinned several transitive dependencies at specific versions: SnakeYAML
> 1.33, Logback 1.2.11, Apache HttpClient 4.5.12, Woodstox 6.0.3,
> json-smart 2.4.8, H2 2.1.214. These are the versions that ship with
> or are commonly used alongside Spring Boot 2.7."

### Step 2: Run Red Hat Dependency Analytics

1. Right-click on `pom.xml` in the VSCode explorer
2. Select **"Red Hat Dependency Analytics Report"**
3. Wait for the scan to complete (typically 15–30 seconds)

> "The Dependency Analytics extension scans every dependency in our pom.xml
> against known CVE databases. Let's see what it finds."

### Step 3: Review the CVE report

4. When the report opens, note the total CVE count
5. Highlight several critical/high-severity CVEs:

> "We're seeing [X] direct vulnerabilities. Let me highlight the ones that
> matter most in an FSI context:
>
> - **SnakeYAML CVE-2022-1471** — this allows arbitrary code execution through
>   crafted YAML input. Our admin config import endpoint parses YAML directly.
>   In a banking context, that's a remote code execution vector.
>
> - **Spring Security CVE-2025-22235** — authorization bypass. We have
>   role-based access control separating admin operations from regular users.
>   This CVE could allow unauthorized access to admin endpoints.
>
> - **Woodstox CVE-2022-40152** — XML External Entity attacks. Our XML
>   statement import feature uses Woodstox to parse bank statements.
>   XXE can lead to data exfiltration.
>
> - **H2 CVE-2022-45868** — remote code execution in the database engine.
>
> Any one of these would be a finding in a PCI-DSS or SOC 2 audit."

### Step 4: (Optional) Show the application is real

If time permits, show the application runs and has real FSI features:

```bash
# In a terminal
mvn -s settings.xml spring-boot:run
```

Then in another terminal (or use the Swagger UI at `http://localhost:8080/swagger-ui.html`):

```bash
# List customers (user credentials)
curl -s -u user:user123 http://localhost:8080/api/customers | jq '.[0:2]'

# Check account balance
curl -s -u user:user123 http://localhost:8080/api/accounts/1/balance | jq

# Transfer funds
curl -s -u user:user123 -X POST http://localhost:8080/api/transfers \
  -H 'Content-Type: application/json' \
  -d '{"sourceAccountId":1,"targetAccountId":2,"amount":100.00,"description":"Demo transfer"}' | jq

# Credit check (exercises Apache HttpClient)
curl -s -u user:user123 -X POST http://localhost:8080/api/customers/1/credit-check | jq

# Admin YAML config import (exercises SnakeYAML) — requires admin role
curl -s -u admin:admin123 -X POST http://localhost:8080/api/admin/config \
  -H 'Content-Type: text/x-yaml' \
  -d 'risk-thresholds:
  low: 720
  medium: 650
  high: 550
compliance:
  framework: PCI-DSS-4.0
  last-audit: 2026-03-15' | jq

# Admin JSON query (exercises json-smart)
curl -s -u admin:admin123 -X POST http://localhost:8080/api/admin/query \
  -H 'Content-Type: application/json' \
  -d '{"data":"{\"accounts\":{\"total\":8,\"active\":8,\"type\":{\"checking\":5,\"savings\":3}}}","expression":"$.accounts.type"}' | jq
```

> "This is a working application with real FSI features — not a hello-world.
> Every one of these features exercises the vulnerable libraries we just saw
> in the scan."

Stop the application: `Ctrl+C`

---

## Part 3: Apply Lightwell Remediation (3 minutes)

### Step 5: Run the remediation script

1. Open the integrated terminal in VSCode
2. Run:

```bash
./apply-rhlw-version.sh
```

Expected output:
```
✅ Applied Lightwell remediated versions to pom.xml

  Spring Boot starters:    2.7.18.rhlw-00004
  spring-security-core:    5.7.11.rhlw-00006
  snakeyaml:               1.33.0.rhlw-00001
  logback-classic:         1.2.11.rhlw-00001
  commons-fileupload:      1.4.0.rhlw-00001
  commons-io:              2.11.0.rhlw-00001
  httpclient:              4.5.12.rhlw-00001
  woodstox-core:           6.0.3.rhlw-00001
  json-smart:              2.4.8.rhlw-00001
  h2:                      2.2.219.rhlw-00001
```

### Step 6: Show what changed in pom.xml

3. Click back to `pom.xml` in the editor — VSCode will show the file has changed
4. Use **Source Control** view (or `git diff`) to show the changes:

> "Look at what changed. Every dependency is still at its original base
> version — 2.7.18, 1.33, 4.5.12, 6.0.3. The only difference is the
> `.rhlw-XXXXX` suffix. That suffix tells Maven to pull the Lightwell
> remediated artifact — same version, with security patches backported.
>
> No `javax` to `jakarta` migration. No Spring Boot 3 upgrade. No code
> changes at all. Just patched artifacts."

### Step 7: Verify the build

```bash
mvn -s settings.xml clean verify -U
```

> "All 80 tests pass. Same application, same behavior, fewer vulnerabilities.
> The `-U` flag forces Maven to pull the remediated artifacts from the
> Lightwell repository."

---

## Part 4: Show the Remediated Scan (3 minutes)

### Step 8: Re-run Dependency Analytics

1. Right-click on `pom.xml` in the explorer
2. Select **"Red Hat Dependency Analytics Report"** again
3. Wait for the new scan to complete

### Step 9: Compare results

> "Now we're seeing [Y] direct vulnerabilities — down from [X]. That's a
> [Z]% reduction in CVE exposure with zero code changes.
>
> The critical CVEs we highlighted earlier — SnakeYAML RCE, Spring Security
> authorization bypass, Woodstox XXE, H2 RCE — are remediated.
>
> From a compliance perspective, we've gone from a report full of critical
> findings to a defensible security posture. Our auditors see patched
> dependencies. Our security team sees a clean scan. And our engineering
> team didn't have to spend months on a framework migration."

---

## Part 5: Key Takeaways (2 minutes)

### Step 10: Revert (optional — show the round-trip)

```bash
./revert-rhlw-version.sh
```

> "And if we ever need to revert — one command, and we're back to upstream
> versions. The process is fully reversible."

### Talking points for wrap-up

> "To summarize what we just saw:
>
> 1. **The problem is real.** Spring Boot 2.7.18 carries multiple known CVEs
>    that auditors and scanners flag. Every one of those CVEs has a real
>    attack surface in this application — YAML parsing, XML parsing, HTTP
>    client calls, JSON processing, database access.
>
> 2. **The upgrade path is expensive.** Moving to Spring Boot 3.x is a major
>    engineering effort with regression risk — especially in regulated
>    environments where re-certification is required.
>
> 3. **Lightwell gives you a third option.** Remediated dependencies that
>    patch CVEs at the artifact level, without changing your base version.
>    You stay on Spring Boot 2.7.18. Your code doesn't change. Your tests
>    still pass. But your CVE exposure drops significantly.
>
> 4. **The process is simple.** One script to apply, one to revert. Standard
>    Maven settings. Works with your existing CI/CD pipeline.
>
> For financial institutions that need to maintain compliance posture while
> managing the cost and risk of framework upgrades, Lightwell bridges the gap."

---

## Demo Timing Summary

| Section | Duration | Cumulative |
|---------|----------|------------|
| Set the scene | 2 min | 2 min |
| Show vulnerable app + scan | 5 min | 7 min |
| Apply remediation | 3 min | 10 min |
| Show remediated scan | 3 min | 13 min |
| Key takeaways | 2 min | 15 min |

Total: ~15 minutes (adjust by skipping or expanding the optional app walkthrough)

---

## Troubleshooting

### Dependency Analytics scan shows "no vulnerabilities"
- Ensure the Red Hat Dependency Analytics extension is installed and authenticated
- Check that you have an active internet connection
- Verify the extension is scanning `pom.xml` (not a generated file)

### Build fails after applying rhlw versions
- Ensure `LW_USERNAME` and `LW_PASSWORD` are exported
- Always use `-s settings.xml` with Maven commands
- Add `-U` flag to force Maven to re-check remote repositories
- If you see "401 Unauthorized", regenerate your Lightwell service account token

### Reverting doesn't restore exact original versions
- The scripts are idempotent — running `revert-rhlw-version.sh` multiple times is safe
- If the pom.xml has been manually edited, the sed patterns may not match — use `git checkout pom.xml` to restore
