#!/usr/bin/env python3

"""
Check that every translatable string resource exists in all supported locales.

For each `values/strings.xml` in the repository, the matching `values-pt-rBR/strings.xml`
and `values-es/strings.xml` must declare exactly the same set of `<string>` and `<plurals>
names. Entries marked `translatable="false"` are ignored everywhere.

Exits with a non-zero status when anything is missing, so it can gate CI.
"""

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

# Locale folders every module must provide alongside the default `values`
REQUIRED_LOCALES = ('values-pt-rBR', 'values-es')

# Directories that never contain source resources
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


def find_default_resource_files(root):
    """Yield every `values/strings.xml` outside of excluded directories."""
    for path in sorted(root.rglob('values/strings.xml')):
        relative_parts = path.relative_to(root).parts
        if any(part in EXCLUDE_DIRS for part in relative_parts):
            continue
        yield path


def read_keys(path):
    """
    Read the translatable resource names declared in a strings.xml file.

    Returns None when the file does not exist, so a missing locale file can be
    reported differently from an empty one.
    """
    if not path.exists():
        return None
    root = ET.parse(path).getroot()
    keys = set()
    for tag in ('string', 'plurals'):
        for element in root.findall(tag):
            if element.get('translatable') == 'false':
                continue
            name = element.get('name')
            if name:
                keys.add(name)
    return keys


def annotate(path, message):
    """Emit a GitHub Actions error annotation when running in CI."""
    if os.environ.get('GITHUB_ACTIONS') == 'true':
        print(f"::error file={path}::{message}")


def check_locale(root, default_path, default_keys, locale):
    """Compare one locale against the default resources, returning a list of problems."""
    locale_path = default_path.parent.parent / locale / 'strings.xml'
    display_path = locale_path.relative_to(root)
    locale_keys = read_keys(locale_path)

    if locale_keys is None:
        message = f"file is missing ({len(default_keys)} untranslated strings)"
        annotate(display_path, message)
        return [f"  {display_path}: {message}"]

    problems = []
    missing = sorted(default_keys - locale_keys)
    if missing:
        message = f"missing translation for: {', '.join(missing)}"
        annotate(display_path, message)
        problems.append(f"  {display_path}: {message}")

    extra = sorted(locale_keys - default_keys)
    if extra:
        message = f"not declared in values/strings.xml: {', '.join(extra)}"
        annotate(display_path, message)
        problems.append(f"  {display_path}: {message}")

    return problems


def main():
    root = find_project_root()
    problems = []
    checked = 0

    for default_path in find_default_resource_files(root):
        default_keys = read_keys(default_path)
        if not default_keys:
            continue
        checked += 1
        for locale in REQUIRED_LOCALES:
            problems.extend(check_locale(root, default_path, default_keys, locale))

    locales = ', '.join(REQUIRED_LOCALES)
    if problems:
        print(f"❌ Translation check failed ({len(problems)} problems):\n")
        print('\n'.join(problems))
        print(f"\nEvery values/strings.xml must have a matching entry in: {locales}")
        return 1

    print(f"✅ {checked} resource files are fully translated into: {locales}")
    return 0


if __name__ == '__main__':
    sys.exit(main())
