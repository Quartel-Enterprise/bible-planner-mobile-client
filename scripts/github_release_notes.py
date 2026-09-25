#!/usr/bin/env python3

"""
Print the Markdown body of a GitHub Release from the in-app release notes.

Usage: github_release_notes.py <version>

Reads the English release notes JSON and renders the entry of the given version
with one section per platform bucket. A pre-release version such as
2.4.0-beta-1 is looked up by its X.Y.Z prefix, since the JSON is keyed by plain
versions. Prints nothing when the version has no entry, so the caller can fall
back to GitHub's generated notes alone.
"""

import json
import re
import sys
from pathlib import Path

RELEASE_NOTES_FILE = Path('feature/release_notes/src/commonMain/composeResources/files/release_notes/en.json')
SECTIONS = (
    ('common', 'All platforms'),
    ('android', 'Android'),
    ('ios', 'iOS'),
    ('desktop', 'Desktop'),
)


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


def render(buckets):
    """Render the platform buckets of one version as Markdown."""
    sections = [(title, buckets[key]) for key, title in SECTIONS if buckets.get(key)]
    lines = ['## Release notes', '']
    for title, items in sections:
        lines.extend([f'### {title}', ''])
        lines.extend(f'- {item}' for item in items)
        lines.append('')
    return '\n'.join(lines)


def main():
    if len(sys.argv) != 2:
        sys.exit('Usage: github_release_notes.py <version>')

    match = re.match(r'^\d+\.\d+\.\d+', sys.argv[1])
    version = match.group(0) if match else sys.argv[1]
    notes = json.loads((find_project_root() / RELEASE_NOTES_FILE).read_text(encoding='utf-8'))
    buckets = notes.get(version)
    if buckets:
        print(render(buckets))


if __name__ == '__main__':
    main()
