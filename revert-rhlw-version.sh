#!/usr/bin/env bash
set -euo pipefail

POM="${1:-pom.xml}"

if [ ! -f "$POM" ]; then
  echo "Error: $POM not found" >&2
  exit 1
fi

# --- Spring Boot starters: strip .rhlw-XXXXX suffix ---
sed -i.bak 's/\${spring\.boot\.version}\.rhlw-[0-9]*/\${spring.boot.version}/g' "$POM"

# --- Transitive dependencies: revert to upstream versions ---
# snakeyaml 1.33.0.rhlw-XXXXX → 1.33
sed -i '' 's|<version>1\.33\.0\.rhlw-[0-9]*</version>|<version>1.33</version>|' "$POM"
# logback-classic 1.2.11.rhlw-XXXXX → 1.2.11
sed -i '' 's|<version>1\.2\.11\.rhlw-[0-9]*</version>|<version>1.2.11</version>|' "$POM"
# spring-security-core 5.7.11.rhlw-XXXXX → 5.7.11
sed -i '' 's|<version>5\.7\.11\.rhlw-[0-9]*</version>|<version>5.7.11</version>|' "$POM"
# commons-fileupload 1.4.0.rhlw-XXXXX → 1.4
sed -i '' 's|<version>1\.4\.0\.rhlw-[0-9]*</version>|<version>1.4</version>|' "$POM"
# commons-io 2.11.0.rhlw-XXXXX → 2.11.0
sed -i '' 's|<version>2\.11\.0\.rhlw-[0-9]*</version>|<version>2.11.0</version>|' "$POM"
# httpclient 4.5.12.rhlw-XXXXX → 4.5.12
sed -i '' 's|<version>4\.5\.12\.rhlw-[0-9]*</version>|<version>4.5.12</version>|' "$POM"
# woodstox-core 6.0.3.rhlw-XXXXX → 6.0.3
sed -i '' 's|<version>6\.0\.3\.rhlw-[0-9]*</version>|<version>6.0.3</version>|' "$POM"
# json-smart 2.4.8.rhlw-XXXXX → 2.4.8
sed -i '' 's|<version>2\.4\.8\.rhlw-[0-9]*</version>|<version>2.4.8</version>|' "$POM"
# h2 2.2.219.rhlw-XXXXX → 2.1.214
sed -i '' 's|<version>2\.2\.219\.rhlw-[0-9]*</version>|<version>2.1.214</version>|' "$POM"

rm -f "${POM}.bak"

echo "✅ Reverted to upstream (non-remediated) versions in $POM"
