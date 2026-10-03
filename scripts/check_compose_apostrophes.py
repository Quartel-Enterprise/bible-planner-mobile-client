#!/usr/bin/env python3

"""
Check that no Compose Resources strings.xml escapes an apostrophe as `\\'`.

Android resources need the backslash, but Compose Multiplatform Resources does not process it: the
backslash reaches the screen and the user reads `doesn\\'t`. Every `composeResources/**/strings.xml`
writes the apostrophe as it is (`doesn't`), in every locale.

Exits with a non-zero status when an escaped apostrophe is found, so it can gate CI.
"""

import sys
from pathlib import Path

ESCAPED_APOSTROPHE = "\\'"

# Directories that never contain source files
EXCLUDE_DIRS = {
    'build', '.gradle', '.kotlin', '.git', '.idea', '.claude',
    'node_modules', 'generated', 'intermediates', 'tmp',
}


def find_project_root():
    """Find the project root by searching upward for `settings.gradle.kts`."""
    current = Path(__file__).resolve().parent
    for _ in range(10):
        if (current / 'settings.gradle.kts').exists():
            return current
        parent = current.parent
        if parent == current:
            break
        current = parent
    return Path(__file__).resolve().parent.parent


def find_compose_strings(root):
    """Yield every strings.xml under a composeResources directory, outside of excluded directories."""
    for path in sorted(root.rglob('strings.xml')):
        parts = path.relative_to(root).parts
        if 'composeResources' in parts and not any(part in EXCLUDE_DIRS for part in parts):
            yield path


def find_escaped_apostrophes(path):
    """Return the `(line number, line)` pairs of every line holding an escaped apostrophe."""
    lines = path.read_text(encoding='utf-8').splitlines()
    return [(number, line.strip()) for number, line in enumerate(lines, start=1) if ESCAPED_APOSTROPHE in line]


def main():
    root = find_project_root()
    files = list(find_compose_strings(root))
    violations = [
        (path, number, line)
        for path in files
        for number, line in find_escaped_apostrophes(path)
    ]

    if violations:
        print(f"❌ Found {len(violations)} escaped apostrophe(s) in Compose Resources strings:")
        print("   Compose Resources shows the backslash on screen; write the apostrophe as is (doesn't).")
        for path, number, line in violations:
            print(f"   {path.relative_to(root)}:{number}: {line}")
        sys.exit(1)

    print(f"✅ No escaped apostrophes in {len(files)} Compose Resources strings.xml file(s)")


if __name__ == '__main__':
    main()
