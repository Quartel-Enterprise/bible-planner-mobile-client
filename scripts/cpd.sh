#!/bin/bash

# Looks for copied code with jscpd (github.com/Quartel-Enterprise/copied-code-check):
#   ./scripts/cpd.sh                                check: fails on copied code .jscpd-baseline.json doesn't list,
#                                                   and on copied code it still lists after it was removed
#   ./scripts/cpd.sh --update-baseline              write the copied code found now to .jscpd-baseline.json, when no
#                                                   file has more of it than the baseline lists, but a moved one
#   ./scripts/cpd.sh --update-baseline --allow-new  also take more copied code in, only when the copy is on purpose
#   ./scripts/cpd.sh --base <baseline of main>      check, and warn about copied code the baseline takes in (CI)
#
# What it scans is in .jscpd.json. This file never changes: the release that the workflows under .github/workflows
# use (Quartel-Enterprise/copied-code-check@X.Y.Z) picks the check and the jscpd it runs, so a local run and CI
# always agree. The release comes from its tag on GitHub, trusted like a GitHub Action pinned to a tag.
#
# Environment:
#   CPD_RUNNER_DIR  a checkout of copied-code-check to run instead of the pinned release (CI, or to try a change)
#   CPD_CACHE_DIR   where releases are cached (default ~/.cache/copied-code-check)
#   CPD_DOWNLOAD_URL  where release tarballs are downloaded from, as <url>/<version> (tests)

set -eo pipefail

ROOT="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." > /dev/null && pwd)"
RUNNER_DIR="$CPD_RUNNER_DIR"

if [ -z "$RUNNER_DIR" ]; then
    # Why: only uses: lines, so a comment or a commented-out step that names the action pins nothing.
    VERSIONS="$( (grep -rhE '^[[:space:]]*(-[[:space:]]+)?uses:[[:space:]]*["'\'']?Quartel-Enterprise/copied-code-check@' \
        "$ROOT/.github/workflows" 2> /dev/null || true) | sed -E 's/.*copied-code-check@([^[:space:]"'\'']*).*/\1/' | sort -u)"

    # A release version, never a branch: its files are cached for good, and release tags never move
    if ! [[ "$VERSIONS" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "❌ The workflows under .github/workflows have to use one release of the check, as"
        echo "   uses: Quartel-Enterprise/copied-code-check@X.Y.Z. Found: '$(echo "$VERSIONS" | xargs)'"
        exit 1
    fi

    CACHE="${CPD_CACHE_DIR:-$HOME/.cache/copied-code-check}"
    RUNNER_DIR="$CACHE/$VERSIONS"
    if [ ! -f "$RUNNER_DIR/scripts/cpd.sh" ]; then
        # A cached release missing its runner was left broken by hand: download it again
        rm -rf "$RUNNER_DIR"
        mkdir -p "$CACHE"
        TMP="$(mktemp -d "$CACHE/.$VERSIONS.XXXXXX")"
        trap 'rm -rf "$TMP"' EXIT
        if ! curl -fsSL "${CPD_DOWNLOAD_URL:-https://codeload.github.com/Quartel-Enterprise/copied-code-check/tar.gz/refs/tags}/$VERSIONS" \
            | tar -xz --strip-components=1 -C "$TMP"; then
            echo "❌ Failed to download the copied-code check $VERSIONS"
            exit 1
        fi
        # Why: a symlink is created only when nothing is at its path, so of two runs caching one release at once,
        # exactly one publishes its download, whole, and the other drops its own.
        # Why: exec below skips the EXIT trap, so the copy that lost is dropped here.
        ln -sn "$TMP" "$RUNNER_DIR" 2> /dev/null || rm -rf "$TMP"
        trap - EXIT
    fi
fi

CPD_PROJECT_ROOT="$ROOT" exec bash "$RUNNER_DIR/scripts/cpd.sh" "$@"
