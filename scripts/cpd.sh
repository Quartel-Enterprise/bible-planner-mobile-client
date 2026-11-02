#!/bin/bash

# Looks for copied code with jscpd (github.com/Quartel-Enterprise/copied-code-check):
#   ./scripts/cpd.sh                                check: fails on copied code .jscpd-baseline.json doesn't list,
#                                                   and on copied code it still lists after it was removed
#   ./scripts/cpd.sh --update-baseline              write the copied code found now to .jscpd-baseline.json, when no
#                                                   file has more of it than the baseline lists, but a moved one
#   ./scripts/cpd.sh --update-baseline --allow-new  also take more copied code in, only when the copy is on purpose
#   ./scripts/cpd.sh --base <baseline of main>      check, and warn about copied code the baseline takes in (what
#                                                   the action runs in CI)
#
# What it scans is in .jscpd.json. This file never changes: the release that every workflow under .github/workflows
# uses (Quartel-Enterprise/copied-code-check@X.Y.Z) picks the check and the jscpd it runs, so a local run and CI
# always agree. The release comes from its tag on GitHub, trusted like a GitHub Action pinned to a tag: releases of
# the repository are immutable, so a tag never moves.
#
# Environment:
#   CPD_RUNNER_DIR  a checkout of copied-code-check to run instead of the pinned release (CI, or to try a change)
#   CPD_EXPECTED_VERSION  with CPD_RUNNER_DIR, the release the workflows must pin (the action passes its own)
#   CPD_CACHE_DIR   where releases are cached (default ~/.cache/copied-code-check)
#   CPD_DOWNLOAD_URL  where release tarballs are downloaded from, as <url>/<version> (tests)

set -eo pipefail

ROOT="$(CDPATH='' cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." > /dev/null && pwd)"
RUNNER_DIR="$CPD_RUNNER_DIR"

# The release the workflows pin, or exits explaining the pin it needs.
# Why: only uses: lines, so a comment or a commented-out step that names the action pins nothing.
pinned_version() {
    local versions
    versions="$( (grep -rhE '^[[:space:]]*(-[[:space:]]+)?uses:[[:space:]]*["'\'']?Quartel-Enterprise/copied-code-check@' \
        "$ROOT/.github/workflows" 2> /dev/null || true) | sed -E 's/.*copied-code-check@([^[:space:]"'\'']*).*/\1/' | sort -u)"

    # A release version, never a branch: its files are cached for good, and release tags never move
    if ! [[ "$versions" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "❌ Every workflow under .github/workflows that uses the check has to pin one release of it, as" >&2
        echo "   uses: Quartel-Enterprise/copied-code-check@X.Y.Z. Found: '$(echo "$versions" | xargs)'" >&2
        exit 1
    fi
    echo "$versions"
}

# Why: in CI the action runs its own files, so the pin a local run reads is checked there, against the action's ref.
if [ -n "$RUNNER_DIR" ] && [ -n "$CPD_EXPECTED_VERSION" ]; then
    VERSION="$(pinned_version)"
    if [ "$VERSION" != "$CPD_EXPECTED_VERSION" ]; then
        echo "❌ The workflows pin the check $VERSION, but CI runs $CPD_EXPECTED_VERSION: a local run would differ"
        exit 1
    fi
fi

if [ -z "$RUNNER_DIR" ]; then
    VERSION="$(pinned_version)"

    if [ -z "$CPD_CACHE_DIR" ] && [ -z "$HOME" ]; then
        echo "❌ No HOME to cache the check in: set CPD_CACHE_DIR"
        exit 1
    fi
    mkdir -p "${CPD_CACHE_DIR:-$HOME/.cache/copied-code-check}"
    # Why: absolute, since the release is published as a symlink, whose relative target would resolve from its own
    # directory.
    CACHE="$(CDPATH='' cd -- "${CPD_CACHE_DIR:-$HOME/.cache/copied-code-check}" > /dev/null && pwd)"
    RUNNER_DIR="$CACHE/$VERSION"

    if [ ! -f "$RUNNER_DIR/scripts/cpd.sh" ]; then
        TMP="$(mktemp -d "$CACHE/.$VERSION.XXXXXX")"
        trap 'rm -rf "$TMP"' EXIT
        if ! curl -fsSL "${CPD_DOWNLOAD_URL:-https://codeload.github.com/Quartel-Enterprise/copied-code-check/tar.gz/refs/tags}/$VERSION" \
            | tar -xz --strip-components=1 -C "$TMP"; then
            echo "❌ Failed to download the copied-code check $VERSION"
            exit 1
        fi

        # Why: checked again after the download, so a release another run published meanwhile is kept, and only an
        # entry still broken (left so by hand) is removed, with the download it points to.
        if [ ! -f "$RUNNER_DIR/scripts/cpd.sh" ] && { [ -e "$RUNNER_DIR" ] || [ -L "$RUNNER_DIR" ]; }; then
            BROKEN="$(readlink "$RUNNER_DIR" || true)"
            rm -rf "$RUNNER_DIR"
            case "$BROKEN" in
                "$CACHE"/.*) rm -rf "$BROKEN" ;;
            esac
        fi

        # Why: a symlink is created only when nothing is at its path, so of two runs caching one release at once,
        # exactly one publishes its download, whole, and the other drops its own (here, as exec skips the trap).
        ln -sn "$TMP" "$RUNNER_DIR" 2> /dev/null || rm -rf "$TMP"
        trap - EXIT
        if [ ! -f "$RUNNER_DIR/scripts/cpd.sh" ]; then
            echo "❌ Couldn't cache the copied-code check in $CACHE: it needs a directory that takes symlinks"
            echo "   (set CPD_CACHE_DIR to another one)"
            exit 1
        fi
    fi
fi

CPD_PROJECT_ROOT="$ROOT" exec bash "$RUNNER_DIR/scripts/cpd.sh" "$@"
