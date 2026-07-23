#!/usr/bin/env bash
set -euo pipefail

POM="${1:-pom.xml}"

if [ ! -f "$POM" ]; then
  echo "Error: $POM not found" >&2
  exit 1
fi

# First revert any existing rhlw suffixes to ensure idempotency
"$(dirname "$0")/revert-rhlw-version.sh" "$POM"

# --- Spring Boot starters: append .rhlw-00004 to ${spring.boot.version} ---
sed -i.bak 's/\${spring\.boot\.version}/\${spring.boot.version}.rhlw-00004/g' "$POM"

# --- Transitive dependencies: apply Lightwell remediated versions ---
# snakeyaml 1.33 → 1.33.0.rhlw-00001
sed -i '' 's|<version>1\.33</version>|<version>1.33.0.rhlw-00001</version>|' "$POM"
# logback-classic 1.2.11 → 1.2.11.rhlw-00001
sed -i '' 's|<version>1\.2\.11</version>|<version>1.2.11.rhlw-00001</version>|' "$POM"
# spring-security-core 5.7.11 → 5.7.11.rhlw-00006
sed -i '' 's|<version>5\.7\.11</version>|<version>5.7.11.rhlw-00006</version>|' "$POM"
# commons-fileupload 1.4 → 1.4.0.rhlw-00001
sed -i '' 's|<version>1\.4</version>|<version>1.4.0.rhlw-00001</version>|' "$POM"
# commons-io 2.11.0 → 2.11.0.rhlw-00001
sed -i '' 's|<version>2\.11\.0</version>|<version>2.11.0.rhlw-00001</version>|' "$POM"
# httpclient 4.5.12 → 4.5.12.rhlw-00001
sed -i '' 's|<version>4\.5\.12</version>|<version>4.5.12.rhlw-00001</version>|' "$POM"
# woodstox-core 6.0.3 → 6.0.3.rhlw-00001
sed -i '' 's|<version>6\.0\.3</version>|<version>6.0.3.rhlw-00001</version>|' "$POM"
# json-smart 2.4.8 → 2.4.8.rhlw-00001
sed -i '' 's|<version>2\.4\.8</version>|<version>2.4.8.rhlw-00001</version>|' "$POM"
# h2 2.1.214 → 2.2.219.rhlw-00001
sed -i '' 's|<version>2\.1\.214</version>|<version>2.2.219.rhlw-00001</version>|' "$POM"

rm -f "${POM}.bak"

echo "✅ Applied Lightwell remediated versions to $POM"
echo ""
echo "  Spring Boot starters:    2.7.18.rhlw-00004"
echo "  spring-security-core:    5.7.11.rhlw-00006"
echo "  snakeyaml:               1.33.0.rhlw-00001"
echo "  logback-classic:         1.2.11.rhlw-00001"
echo "  commons-fileupload:      1.4.0.rhlw-00001"
echo "  commons-io:              2.11.0.rhlw-00001"
echo "  httpclient:              4.5.12.rhlw-00001"
echo "  woodstox-core:           6.0.3.rhlw-00001"
echo "  json-smart:              2.4.8.rhlw-00001"
echo "  h2:                      2.2.219.rhlw-00001"
