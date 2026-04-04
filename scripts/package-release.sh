#!/usr/bin/env bash
# Usage: ./scripts/package-release.sh <version>
# Prereq: master worktree = $PWD, beta worktree = ../forge-beta, both built
set -euo pipefail

VERSION="${1:?Usage: $0 <version>}"
STAGING="/tmp/forge-release-$VERSION"
OUT="/tmp/Forge-$VERSION.zip"

echo "==> Cleaning staging: $STAGING"
rm -rf "$STAGING"
mkdir -p "$STAGING/forge"

echo "==> Extracting stable tarball"
STABLE_TAR=$(ls forge-installer/target/forge-installer-*.tar.bz2 2>/dev/null | head -1)
if [ -z "$STABLE_TAR" ]; then
    echo "ERROR: no stable tarball. Run 'mvn -B -P windows-linux install -DskipTests' first."
    exit 1
fi
tar -xjf "$STABLE_TAR" -C "$STAGING/forge" --strip-components 1

echo "==> Copying beta jar"
BETA_JAR=$(ls ../forge-beta/forge-installer/target/forge-installer-*-SNAPSHOT/forge.jar 2>/dev/null | head -1)
if [ -z "$BETA_JAR" ]; then
    echo "ERROR: no beta jar. Build ../forge-beta first."
    exit 1
fi
cp "$BETA_JAR" "$STAGING/forge/forge-beta.jar"

echo "==> Generating forge-beta launchers"
for ext in command sh; do
    if [ -f "$STAGING/forge/forge.$ext" ]; then
        sed -e 's/forge\.jar/forge-beta.jar/g' \
            -e 's|java |java -Dforge.commander.enhanced=true |g' \
            "$STAGING/forge/forge.$ext" > "$STAGING/forge/forge-beta.$ext"
        chmod +x "$STAGING/forge/forge-beta.$ext"
    fi
done
if [ -f "$STAGING/forge/forge.exe" ]; then
    cp "$STAGING/forge/forge.exe" "$STAGING/forge/forge-beta.exe"
    echo "NOTE: forge-beta.exe points to forge.jar -- VM prop not injected on Windows."
fi

echo "==> Creating release zip"
cd "$STAGING"
zip -rq "$OUT" forge
echo "==> Done: $OUT"
ls -lh "$OUT"
