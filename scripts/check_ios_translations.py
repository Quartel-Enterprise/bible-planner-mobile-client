#!/usr/bin/env python3

"""
Check that every iOS localized table exists in all supported languages with the same keys.

For each `.strings` and `.stringsdict` table in `iosApp/iosApp/en.lproj`, the matching tables in
`pt-BR.lproj` and `es.lproj` must declare exactly the same keys, and each key must use the same
format specifiers (`%ld`, `%1$@`...) in every language, so a translation can't drop or swap an argument.
A `.stringsdict` key is compared on every plural variant. `InfoPlist.strings` is checked too.

Usage: python3 scripts/check_ios_translations.py [project root]
Exits with a non-zero status when anything is missing, so it can gate CI.
"""

import os
import plistlib
import re
import sys
from pathlib import Path

BASE_LANGUAGE = 'en'
REQUIRED_LANGUAGES = ('pt-BR', 'es')
APP_DIR = Path('iosApp/iosApp')

# `%%` is a literal percent sign; everything else that starts with `%` is an argument
FORMAT_SPECIFIER = re.compile(r'%(?:\d+\$)?[-+ #0]*\d*(?:\.\d+)?(?:ll|l|h|hh|q|z|t|j)?[@dDuUxXoOfeEgGcCsSp]')
# Keys may be quoted or bare identifiers, as in `InfoPlist.strings` (`CFBundleDisplayName = "...";`)
STRINGS_ENTRY = re.compile(r'(?:"((?:[^"\\]|\\.)*)"|([A-Za-z0-9_.-]+))\s*=\s*"((?:[^"\\]|\\.)*)"\s*;')
COMMENTS = re.compile(r'/\*.*?\*/|//[^\n]*', re.DOTALL)


def specifiers(text):
    return sorted(FORMAT_SPECIFIER.findall(text.replace('%%', '')))


def read_strings(path):
    """Map each key of a `.strings` file to the sorted format specifiers of its value."""
    content = COMMENTS.sub('', path.read_text(encoding='utf-8'))
    entries = {}
    for line in content.splitlines():
        line = line.strip()
        if not line:
            continue
        match = STRINGS_ENTRY.fullmatch(line)
        if not match:
            raise ValueError(f"can't parse line: {line}")
        key = match.group(1) if match.group(1) is not None else match.group(2)
        entries[key] = specifiers(match.group(3))
    return entries


def read_stringsdict(path):
    """Map each key of a `.stringsdict` file to the format specifiers of each plural variant."""
    with path.open('rb') as file:
        plist = plistlib.load(file)
    entries = {}
    for key, rule in plist.items():
        variants = {}
        for name, value in rule.items():
            if isinstance(value, dict):
                for quantity, text in value.items():
                    if not quantity.startswith('NSString'):
                        variants[f'{name}.{quantity}'] = specifiers(text)
        entries[key] = variants
    return entries


def read_table(path):
    return read_stringsdict(path) if path.suffix == '.stringsdict' else read_strings(path)


def annotate(path, message):
    """Emit a GitHub Actions error annotation when running in CI."""
    if os.environ.get('GITHUB_ACTIONS') == 'true':
        print(f"::error file={path}::{message}")


def parse_problem(root, path, error):
    display_path = path.relative_to(root)
    annotate(display_path, str(error))
    return f"  {display_path}: {error}"


def check_table(root, base, base_path, language):
    path = base_path.parent.parent / f'{language}.lproj' / base_path.name
    display_path = path.relative_to(root)
    if not path.exists():
        message = f"table is missing ({len(base)} keys)"
        annotate(display_path, message)
        return [f"  {display_path}: {message}"]
    try:
        translated = read_table(path)
    except ValueError as error:
        return [parse_problem(root, path, error)]
    problems = []
    for label, keys in (
        ('missing translation for', sorted(base.keys() - translated.keys())),
        (f'not declared in {BASE_LANGUAGE}.lproj', sorted(translated.keys() - base.keys())),
    ):
        if keys:
            message = f"{label}: {', '.join(keys)}"
            annotate(display_path, message)
            problems.append(f"  {display_path}: {message}")
    for key in sorted(base.keys() & translated.keys()):
        if base[key] != translated[key]:
            message = f"format specifiers of '{key}' differ: {base[key]} in {BASE_LANGUAGE}, {translated[key]} here"
            annotate(display_path, message)
            problems.append(f"  {display_path}: {message}")
    return problems


def main():
    root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parent.parent
    base_dir = root / APP_DIR / f'{BASE_LANGUAGE}.lproj'
    tables = sorted(path for path in base_dir.iterdir() if path.suffix in ('.strings', '.stringsdict'))
    problems = []
    for table in tables:
        try:
            base = read_table(table)
        except ValueError as error:
            problems.append(parse_problem(root, table, error))
            continue
        for language in REQUIRED_LANGUAGES:
            problems.extend(check_table(root, base, table, language))
    languages = ', '.join(REQUIRED_LANGUAGES)
    if problems:
        print(f"❌ iOS translation check failed ({len(problems)} problems):\n")
        print('\n'.join(problems))
        print(f"\nEvery table in {APP_DIR}/{BASE_LANGUAGE}.lproj must match in: {languages}")
        return 1
    print(f"✅ {len(tables)} iOS tables are fully translated into: {languages}")
    return 0


if __name__ == '__main__':
    sys.exit(main())
