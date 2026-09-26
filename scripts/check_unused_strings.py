#!/usr/bin/env python3

"""
Check that every string resource declared in the default locale is referenced by the code.

Compose Resources: each `<module>/src/<sourceSet>/composeResources/values/strings.xml` generates
one accessor per `<string>`/`<plurals>` in the package `bibleplanner.<module path>.generated.resources`.
Accessors are extension properties on `Res.string`/`Res.plurals`, so any file that uses one has to
import it by name (`import bibleplanner.feature.day.generated.resources.day_title`). A resource is
used when such an import exists anywhere in the repository, which also covers modules exposing
their `Res` class to other modules through `publicResClass`.

Android resources: each `<module>/src/<sourceSet>/res/values/strings.xml` is used when some Kotlin
code references `R.string.<name>`/`R.plurals.<name>`, or some XML references `@string/<name>`.

Android Lint's `UnusedResources` can't do this job: it doesn't see Compose Resources accessors.

Exits with a non-zero status when anything is unused, so it can gate CI.
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

# Root package every module's generated `Res` class lives under
RES_PACKAGE_ROOT = 'bibleplanner'

# Android resources the build or the platform reads by name, never through code
ANDROID_RESERVED_NAMES = {'app_name'}

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


def is_excluded(root, path):
    return any(part in EXCLUDE_DIRS for part in path.relative_to(root).parts)


def find_files(root, pattern):
    """Yield every file matching `pattern` outside of excluded directories."""
    for path in sorted(root.rglob(pattern)):
        if not is_excluded(root, path):
            yield path


def read_resources(path):
    """Return the `(kind, name)` pairs of every `<string>`/`<plurals>` declared in a strings.xml."""
    resources = []
    for element in ET.parse(path).getroot():
        name = element.get('name')
        if element.tag in ('string', 'plurals') and name:
            resources.append((element.tag, name))
    return resources


def module_dir(strings_path):
    """Return the module directory owning `<module>/src/<sourceSet>/<kind>/values/strings.xml`."""
    return strings_path.parents[4]


def compose_package(root, strings_path):
    relative = module_dir(strings_path).relative_to(root)
    return '.'.join((RES_PACKAGE_ROOT, *relative.parts, 'generated', 'resources'))


def annotate(path, message):
    """Emit a GitHub Actions error annotation when running in CI."""
    if os.environ.get('GITHUB_ACTIONS') == 'true':
        print(f"::error file={path}::{message}")


def main():
    root = find_project_root()
    kotlin_sources = '\n'.join(
        path.read_text(encoding='utf-8', errors='ignore') for path in find_files(root, '*.kt')
    )
    xml_sources = '\n'.join(
        path.read_text(encoding='utf-8', errors='ignore')
        for path in find_files(root, '*.xml')
        if path.parent.name != 'values' and not path.parent.name.startswith('values-')
    )
    compose_imports = set(re.findall(r'^import\s+([\w.]+)', kotlin_sources, re.MULTILINE))

    problems = []
    checked = 0

    for strings_path in find_files(root, 'composeResources/values/strings.xml'):
        package = compose_package(root, strings_path)
        unused = [
            name for _, name in read_resources(strings_path)
            if f'{package}.{name}' not in compose_imports
        ]
        checked += 1
        if unused:
            problems.append((strings_path.relative_to(root), unused))

    for strings_path in find_files(root, 'res/values/strings.xml'):
        unused = [
            name for kind, name in read_resources(strings_path)
            if name not in ANDROID_RESERVED_NAMES
            and not re.search(rf'\bR\.{kind}\.{re.escape(name)}\b', kotlin_sources)
            and not re.search(rf'@string/{re.escape(name)}\b', xml_sources)
        ]
        checked += 1
        if unused:
            problems.append((strings_path.relative_to(root), unused))

    if problems:
        count = sum(len(unused) for _, unused in problems)
        print(f"❌ Unused strings check failed ({count} unused strings):\n")
        for path, unused in problems:
            message = f"never referenced: {', '.join(unused)}"
            annotate(path, message)
            print(f"  {path}: {message}")
        print("\nDelete them from values/strings.xml and from every translation next to it.")
        return 1

    print(f"✅ Every string in {checked} resource files is referenced by the code")
    return 0


if __name__ == '__main__':
    sys.exit(main())
