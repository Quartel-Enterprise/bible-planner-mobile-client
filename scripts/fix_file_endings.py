#!/usr/bin/env python3

import os
import sys
import argparse
from pathlib import Path

# File extensions to process
SOURCE_EXTENSIONS = (
    '.kt', '.kts',           # Kotlin
    '.swift',                # Swift
    '.h', '.m',              # Objective-C
    '.java',                 # Java
    '.xml',                  # XML
    '.json',                 # JSON
    '.md',                   # Markdown
    '.properties',           # Properties
    '.toml',                 # TOML
    '.pro',                  # ProGuard
    '.xcconfig',             # Xcode config
    '.gradle',               # Gradle
    '.yml', '.yaml',         # YAML
    '.sh', '.bash',          # Shell scripts
    '.py',                   # Python
    '.js', '.ts',            # JavaScript/TypeScript
    '.css', '.scss',         # Styles
    '.html',                 # HTML
)

# Directories to exclude
EXCLUDE_DIRS = {
    'build', '.gradle', 'xcuserdata', '.idea', '.git',
    'node_modules', 'dist', 'out', '.DS_Store',
    'generated', '.kotlin', 'intermediates', 'tmp',
    '__pycache__', '.cache', 'venv', 'env'
}


def find_project_root():
    """
    Find the project root directory by looking for marker files.
    Searches upward from the script's directory.

    Returns:
        Path to project root, or current directory if not found
    """
    # Start from the script's directory
    current = Path(__file__).resolve().parent

    # Project root markers (in order of preference)
    root_markers = [
        'settings.gradle.kts',  # Gradle multi-project
        '.git',                 # Git repository
        'build.gradle.kts',     # Gradle project
        'build.gradle',         # Gradle project (Groovy)
        'package.json',         # Node.js project
        'pyproject.toml',       # Python project
        'Cargo.toml',           # Rust project
    ]

    # Search upward for project root
    max_depth = 10  # Prevent infinite loop
    for _ in range(max_depth):
        for marker in root_markers:
            if (current / marker).exists():
                return current

        # Move up one directory
        parent = current.parent
        if parent == current:  # Reached filesystem root
            break
        current = parent

    # If not found, return the parent of the scripts directory
    # (assuming scripts/ is in the project root)
    script_parent = Path(__file__).resolve().parent.parent
    if script_parent.exists():
        return script_parent

    # Fallback to current working directory
    return Path.cwd()


def is_binary_file(filepath):
    """Check if a file is binary by reading first 8192 bytes."""
    try:
        with open(filepath, 'rb') as f:
            chunk = f.read(8192)
            if b'\x00' in chunk:  # Null byte indicates binary
                return True
        return False
    except Exception:
        return True


def should_process_file(filepath):
    """Determine if a file should be processed."""
    # Check if it's in an excluded directory
    parts = Path(filepath).parts
    if any(excluded in parts for excluded in EXCLUDE_DIRS):
        return False

    # Check file extension
    if not filepath.endswith(SOURCE_EXTENSIONS):
        return False

    # Skip binary files
    if is_binary_file(filepath):
        return False

    return True


def fix_file_ending(filepath, check_only=False):
    """
    Fix file to end with exactly one newline.

    Returns:
        'fixed' if file was modified
        'ok' if file was already correct
        'skipped' if file was skipped
        'error' if there was an error
    """
    try:
        with open(filepath, 'rb') as f:
            content = f.read()

        if len(content) == 0:
            return 'skipped'

        # Remove all trailing whitespace and add exactly one newline
        new_content = content.rstrip() + b'\n'

        if content != new_content:
            if not check_only:
                with open(filepath, 'wb') as f:
                    f.write(new_content)
                return 'fixed'
            else:
                return 'needs_fix'
        else:
            return 'ok'

    except Exception as e:
        print(f"Error processing {filepath}: {e}", file=sys.stderr)
        return 'error'


def process_directory(directory, check_only=False, verbose=False):
    """Process all files in a directory and its subdirectories."""
    directory = Path(directory).resolve()

    if not directory.exists():
        print(f"Error: Directory '{directory}' does not exist", file=sys.stderr)
        return 1

    if not directory.is_dir():
        print(f"Error: '{directory}' is not a directory", file=sys.stderr)
        return 1

    stats = {
        'fixed': 0,
        'needs_fix': 0,
        'ok': 0,
        'skipped': 0,
        'error': 0,
        'total': 0
    }

    files_needing_fix = []

    print(f"{'Checking' if check_only else 'Processing'} files in: {directory}")
    print()

    # Walk through directory
    for root, dirs, files in os.walk(directory):
        # Remove excluded directories from traversal
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

        for filename in files:
            filepath = os.path.join(root, filename)

            if should_process_file(filepath):
                stats['total'] += 1
                result = fix_file_ending(filepath, check_only)
                stats[result] += 1

                if verbose or result in ('fixed', 'needs_fix', 'error'):
                    relative_path = os.path.relpath(filepath, directory)

                    if result == 'fixed':
                        print(f"✓ Fixed: {relative_path}")
                    elif result == 'needs_fix':
                        print(f"✗ Needs fix: {relative_path}")
                        files_needing_fix.append(relative_path)
                    elif result == 'error':
                        print(f"⚠ Error: {relative_path}")

    # Print summary
    print()
    print("=" * 60)
    print("Summary:")
    print("=" * 60)
    print(f"Total files processed: {stats['total']}")

    if check_only:
        print(f"✓ Files already correct: {stats['ok']}")
        print(f"✗ Files needing fix: {stats['needs_fix']}")
        if stats['error'] > 0:
            print(f"⚠ Files with errors: {stats['error']}")

        if stats['needs_fix'] > 0:
            print()
            print(f"Run without --check to fix {stats['needs_fix']} files")
            return 1
        else:
            print()
            print("All files end with exactly one newline! ✓")
            return 0
    else:
        print(f"✓ Files fixed: {stats['fixed']}")
        print(f"✓ Files already correct: {stats['ok']}")
        if stats['error'] > 0:
            print(f"⚠ Files with errors: {stats['error']}")
        print()

        if stats['fixed'] > 0:
            print(f"Successfully fixed {stats['fixed']} files! ✓")
        else:
            print("All files were already correct! ✓")

        return 0


def main():
    parser = argparse.ArgumentParser(
        description='Ensure all source files end with exactly one newline',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s                        # Fix files in project root (auto-detected)
  %(prog)s /path/to/project       # Fix files in specific directory
  %(prog)s --check                # Check without fixing
  %(prog)s --verbose              # Show all files processed
        """
    )

    parser.add_argument(
        'directory',
        nargs='?',
        default=None,
        help='Directory to process (default: auto-detected project root)'
    )

    parser.add_argument(
        '--check',
        action='store_true',
        help='Check files without modifying them'
    )

    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Show all files being processed'
    )

    args = parser.parse_args()

    # Use project root if no directory specified
    if args.directory is None:
        args.directory = find_project_root()

    return process_directory(args.directory, args.check, args.verbose)


if __name__ == '__main__':
    sys.exit(main())
