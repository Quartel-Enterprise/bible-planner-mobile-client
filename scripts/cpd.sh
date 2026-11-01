#!/bin/bash

# Looks for copied code in the Kotlin and Swift of the apps with jscpd (github.com/kucherenko/jscpd):
#   ./scripts/cpd.sh                                check: fails on copied code .jscpd-baseline.json doesn't list,
#                                                   and on copied code it still lists after it was removed
#   ./scripts/cpd.sh --update-baseline              write the copied code found now to .jscpd-baseline.json, when
#                                                   there is no more of it than the baseline lists (a renamed file)
#   ./scripts/cpd.sh --update-baseline --allow-new  also take more copied code in, only when the copy is on purpose
#
# What it scans is in .jscpd.json. jscpd only compares files of the same language, so a screen drawn by Compose
# and by SwiftUI is never a clone: what it finds is logic or UI copied within one of them.

set -e

ROOT="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." > /dev/null && pwd)"
cd "$ROOT"

if ! command -v npm > /dev/null || ! command -v node > /dev/null || [ "$(node -p 'process.versions.node.split(".")[0]')" -lt 18 ]; then
    echo "❌ jscpd needs Node.js 18 or later (brew install node)"
    exit 1
fi

# The release scripts/jscpd/package.json pins, with every dependency its lockfile locks. Installed again only when
# the lockfile changed since the last install.
TOOL="scripts/jscpd"
if ! cmp -s "$TOOL/package-lock.json" "$TOOL/node_modules/.installed-lock.json"; then
    npm ci --prefix "$TOOL" --no-audit --no-fund --loglevel=error
    cp "$TOOL/package-lock.json" "$TOOL/node_modules/.installed-lock.json"
fi

rm -rf build/jscpd
if ! "$TOOL/node_modules/.bin/jscpd" --config .jscpd.json; then
    echo "❌ jscpd failed: set \"silent\" to false in .jscpd.json to see why"
    exit 1
fi

exec node scripts/cpd-check.mjs build/jscpd/jscpd-report.json .jscpd-baseline.json "$@"
