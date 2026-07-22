#!/usr/bin/env bash
set -euo pipefail

POM="${1:-pom.xml}"

if [ ! -f "$POM" ]; then
  echo "Error: $POM not found" >&2
  exit 1
fi

# Strip any .rhlw-XXXXX suffix from ${spring.boot.version} references
sed -i.bak 's/\${spring\.boot\.version}\.rhlw-[0-9]*/\${spring.boot.version}/g' "$POM"

rm -f "${POM}.bak"

echo "Reverted all \${spring.boot.version} references to bare version in $POM"
