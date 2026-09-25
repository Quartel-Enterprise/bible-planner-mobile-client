#!/usr/bin/env python3

"""
Check that the in-app release notes JSON files are well-formed and in sync.

Every file maps a plain X.Y.Z version to an object of platform buckets:

    {"2.9.0": {"common": ["..."], "ios": ["..."]}}

The app shows `common` plus the bucket of the platform it runs on, and the store
pipeline (fastlane/Fastfile) does the same for each store. This script guards:

- only the known buckets are used, each a non-empty list of non-empty strings;
- no version is left without any note;
- en, pt and es declare the same versions, the same buckets per version and the
  same number of notes per bucket, so no language misses a translation.

Exits with a non-zero status when anything is wrong, so it can gate CI.
"""

import json
import re
import sys
from pathlib import Path

RELEASE_NOTES_DIR = Path('feature/release_notes/src/commonMain/composeResources/files/release_notes')
LANGUAGES = ('en', 'pt', 'es')
REFERENCE_LANGUAGE = 'en'
BUCKETS = ('common', 'android', 'ios', 'desktop')
VERSION_PATTERN = re.compile(r'^\d+\.\d+\.\d+$')


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


def check_structure(language, notes):
    """Return the structural problems of a single language file."""
    errors = []
    if not isinstance(notes, dict):
        return [f'{language}.json: the root must be an object keyed by version']

    for version, buckets in notes.items():
        where = f'{language}.json › {version}'
        if not VERSION_PATTERN.match(version):
            errors.append(f'{where}: the version key must be a plain X.Y.Z')
        if not isinstance(buckets, dict):
            errors.append(f'{where}: must be an object of platform buckets {list(BUCKETS)}')
            continue
        if not buckets:
            errors.append(f'{where}: has no notes — remove the version or add a note')
        for bucket, items in buckets.items():
            if bucket not in BUCKETS:
                errors.append(f'{where}: unknown bucket "{bucket}" (allowed: {", ".join(BUCKETS)})')
                continue
            if not isinstance(items, list) or not items:
                errors.append(f'{where} › {bucket}: must be a non-empty list — omit the bucket instead')
                continue
            for index, item in enumerate(items):
                if not isinstance(item, str) or not item.strip():
                    errors.append(f'{where} › {bucket}[{index}]: must be a non-empty string')
    return errors


def describe_shape(notes):
    """Map every version to its {bucket: note count} shape."""
    return {
        version: {
            bucket: len(items)
            for bucket, items in buckets.items()
            if isinstance(items, list)
        }
        for version, buckets in notes.items()
        if isinstance(buckets, dict)
    }


def check_sync(shapes):
    """Return the differences between each language and the reference one."""
    errors = []
    reference = shapes[REFERENCE_LANGUAGE]
    for language, shape in shapes.items():
        if language == REFERENCE_LANGUAGE:
            continue
        for version in reference.keys() - shape.keys():
            errors.append(f'{language}.json: missing version {version}')
        for version in shape.keys() - reference.keys():
            errors.append(f'{language}.json: version {version} is not in {REFERENCE_LANGUAGE}.json')
        for version in reference.keys() & shape.keys():
            if reference[version] != shape[version]:
                errors.append(
                    f'{language}.json › {version}: buckets {shape[version]} differ from '
                    f'{REFERENCE_LANGUAGE}.json {reference[version]}'
                )
    return errors


def main():
    root = find_project_root()
    errors = []
    shapes = {}

    for language in LANGUAGES:
        path = root / RELEASE_NOTES_DIR / f'{language}.json'
        if not path.exists():
            errors.append(f'{path.relative_to(root)}: file not found')
            continue
        try:
            notes = json.loads(path.read_text(encoding='utf-8'))
        except json.JSONDecodeError as error:
            errors.append(f'{language}.json: invalid JSON — {error}')
            continue
        structure_errors = check_structure(language, notes)
        errors.extend(structure_errors)
        if not structure_errors:
            shapes[language] = describe_shape(notes)

    if len(shapes) == len(LANGUAGES):
        errors.extend(check_sync(shapes))

    if errors:
        print('Release notes check failed:\n')
        for error in sorted(errors):
            print(f'  - {error}')
        print(f'\n{len(errors)} problem(s) found in {RELEASE_NOTES_DIR}.')
        sys.exit(1)

    print(f'Release notes OK: {len(shapes[REFERENCE_LANGUAGE])} versions in sync across {", ".join(LANGUAGES)}.')


if __name__ == '__main__':
    main()
