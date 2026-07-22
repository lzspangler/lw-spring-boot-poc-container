#!/usr/bin/env bash
set -euo pipefail

POM="${1:-pom.xml}"
SUFFIX="rhlw-00004"

if [ ! -f "$POM" ]; then
  echo "Error: $POM not found" >&2
  exit 1
fi

# Strip any existing .rhlw-XXXXX suffix from ${spring.boot.version} references
sed -i.bak 's/\${spring\.boot\.version}\.rhlw-[0-9]*/\${spring.boot.version}/g' "$POM"

# Append .rhlw-00004 to all ${spring.boot.version} references
sed -i '' 's/\${spring\.boot\.version}/\${spring.boot.version}.'"$SUFFIX"'/g' "$POM"

rm -f "${POM}.bak"

echo "Applied .${SUFFIX} suffix to all \${spring.boot.version} references in $POM"
