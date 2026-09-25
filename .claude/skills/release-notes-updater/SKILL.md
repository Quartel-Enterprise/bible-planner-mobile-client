---
name: release-notes-updater
description: "Update the pt.json, en.json, and es.json release notes files with user-friendly descriptions of what changed in the new app version."
---

# Release Notes Updater

This skill updates the three release notes JSON files (`pt.json`, `en.json`, `es.json`) that are
displayed directly to users inside the app. The notes need to be clear and friendly — avoid jargon
and write as if explaining to someone who uses the app but knows nothing about software mainment.

## JSON file locations

The source files are always at:
```
feature/release_notes/src/commonMain/composeResources/files/release_notes/pt.json
feature/release_notes/src/commonMain/composeResources/files/release_notes/en.json
feature/release_notes/src/commonMain/composeResources/files/release_notes/es.json
```

## JSON structure

Each file is a JSON object where:
- Keys are version strings like `"1.13.0"`, ordered from newest to oldest
- Values are objects of **platform buckets**, each an array of user-facing strings:
  - `common` — changes every user sees, on any platform
  - `android` — changes only Android users see
  - `ios` — changes only iPhone/iPad users see
  - `desktop` — changes only desktop (macOS, Windows, Linux) users see
- Buckets are optional: omit a bucket that has no notes (never write an empty array), and write
  them in the order `common`, `android`, `ios`, `desktop`

```json
{
  "2.9.0": {
    "common": [
      "On the Profile tab, the list no longer jumps back to the top when you return from another screen."
    ],
    "ios": [
      "The bottom bar with the Plans, Books and Profile tabs is now the system's own tab bar."
    ]
  },
  "2.8.4": {
    "android": [
      "Fixed a problem that could rarely close the app on its own while moving between screens."
    ]
  }
}
```

Each platform shows `common` followed by its own bucket — in the app's release notes screen and in
the store "What's New" (Google Play gets `common` + `android`, the App Store gets `common` + `ios`).
The GitHub Release lists every bucket in its own section. A version whose notes all target other
platforms is hidden in the app and gets a generic "Bug fixes and performance improvements" in the
store of the platform it has nothing for.

`scripts/check_release_notes.py` validates the three files in CI (known buckets only, no empty
bucket or version, same versions/buckets/item counts in every language). Run it after editing:

```bash
python3 scripts/check_release_notes.py
```

## Step-by-step workflow

### 1. Find the latest published release via git tag

Run the following command in the project root to get the latest version tag:

```bash
git tag --sort=-version:refname | grep -E '^v?[0-9]+\.[0-9]+\.[0-9]+$' | head -1
```

This returns the highest semver tag (e.g. `v1.12.0` or `1.12.0`).
Strip the leading `"v"` if present to get a clean version like `"1.12.0"`.

If the command fails or returns nothing (no tags yet), ask the user:
> "No version tags found in the repository. What is the latest released version?"

### 2. Read the JSON files and find the highest version

Read all three JSON files. Parse the keys as semver versions and find the highest one across the files.
A version `A` is higher than version `B` if, comparing major.minor.patch numerically, any component
of `A` is greater at the first differing position.

### 3. Decide whether to create a new version entry or use an existing one

**Case A — The highest version in the JSONs is already greater than the latest git tag:**
A "next version" slot already exists (e.g. `"1.13.0"` exists while the last tag is `"1.12.0"`).
Use that version. If it has no notes yet or the user wants to add more items, proceed to step 4.

**Case B — The highest version in the JSONs is equal to or less than the latest git tag:**
There is no next-version slot yet. Ask the user:
> "What kind of update is this new version?"

Options:
- **New feature** (minor) — new functionality added to the app
- **Bug fix** (patch) — fixes only, no new features
- **Major update** (major) — significant changes to how the app works

Then compute the new version string by bumping the corresponding semver component of the latest
tag (major → X+1.0.0, minor → X.Y+1.0, patch → X.Y.Z+1).

### 4. Collect the release notes

First, try to infer the changes automatically by running these commands in the project root:

```bash
# Sync remote refs
git remote update

# Committed changes not yet in origin/main
git log origin/main..HEAD --oneline

# Uncommitted changes (staged and unstaged)
git diff HEAD
```

Use the commit messages and diff output to figure out what changed from a user's perspective —
and **on which platforms** (see step 5).
If you can extract meaningful changes from this output, proceed directly to step 5 — do not ask the user.

Only ask the user if:
- Both commands return nothing (no commits ahead of origin/main and no uncommitted changes), or
- The output is too ambiguous to determine what actually changed for the user

In that case, ask:
> "What changed in this version? Describe it simply, as you would explain it to someone who just uses the app."

If the user already described the changes earlier in the conversation, use that — don't run the commands or ask again.

### 5. Classify each change by platform

Put each change in the bucket of the platforms whose users actually notice it:

- **`common`** — the default: shared UI and behavior in `commonMain`, backend/sync changes, anything
  every platform ships. Changes tied to screen size (tablets, landscape, large screens) are `common`
  too, since they reach Android tablets, iPads and desktop alike.
- **`android`** — code only in `androidMain`, `androidApp/`, or Android-only APIs/services (Google Play
  in-app review/updates, Credential Manager, dynamic colors, themed icons, system navigation bar).
- **`ios`** — code only in `iosMain`, `iosApp/`, Swift, or iOS-only UI (native iOS components, SF
  Symbols, Dynamic Island, Liquid Glass).
- **`desktop`** — code only in `jvmMain` or `desktopApp/` (window, installer, mouse/keyboard-only
  behavior).

The changed file paths in the diff are the strongest hint. When a change reaches exactly two
platforms, write it in both buckets, each phrased for its own audience (e.g. "With a mouse connected,
…" under `android` and "You can now use your mouse's side buttons…" under `desktop`).

### 6. Write user-friendly notes in three languages

Transform the user's description into polished, user-facing sentences. The golden rules:

- **No jargon**: say "search bar" not "SearchBar component"; say "books screen" not "BooksScreen"; say "fixed a problem" not "bug fix" or "null pointer exception"
- **Short and clear**: one sentence per change, starting with an action verb
- **User's perspective**: describe what they *experience*, not what changed in the code
- **Positive framing**: "You can now..." or "Fixed an issue that..." instead of dry technical descriptions
- **Omit internal changes**: refactors, architecture changes, and dependency updates that have no visible user impact should be skipped entirely
- **No redundant platform prefix**: a note in a platform bucket is only shown on that platform, so
  don't open it with "On Android," / "On iPhone," — keep a platform word only when it narrows the
  change further (e.g. "On iPhone in landscape," when iPad isn't affected)

Write the notes in all three languages:
- **pt** (Portuguese — Brazil)
- **en** (English)
- **es** (Spanish — Latin America)

Read a few existing entries in each file to calibrate the style and tone before writing.

### 7. Update the JSON files

- Place the version entry at the **top** of each JSON file (versions are ordered newest → oldest)
- If the version slot already existed with some notes, **append** the new items to the existing
  array of the right bucket, creating the bucket if it doesn't exist yet
- Edit all three files: `pt.json`, `en.json`, `es.json` — the same buckets with the same number of
  items in each
- Run `python3 scripts/check_release_notes.py` and fix anything it reports
- Confirm briefly to the user what was added, in which version and in which bucket

## Example transformations

| Developer says (raw) | Bucket | Written in the JSON |
|---|---|---|
| "removed extra whitespace below the search bar on the books screen" | `common` | "Fixed extra whitespace that appeared below the search bar on the books screen." |
| "added download progress notifications for bible versions" | `common` | "You now receive notifications about the download progress of Bible versions." |
| "refactored MoreScreen for better usability" | `common` | "The 'More' screen has been reorganized to be easier to use." |
| "fix null pointer on offline mode" | `common` | "Fixed an issue that could crash the app when used without an internet connection." |
| "dynamic colors off by default (androidMain)" | `android` | "Dynamic colors, which tint the app with the colors of your wallpaper, now come turned off." |
| "use native UITabBar via Calf on iOS" | `ios` | "The bottom bar with the tabs is now the system's own tab bar." |
| "migrated ViewModel from LiveData to StateFlow" | — | *(omit — internal change with no visible user impact)* |

## Edge cases

- If the user provides notes for multiple changes at once, create one array item per change
- If a version slot exists in `en.json` but not in `pt.json` or `es.json`, create it in the missing files too
- If a change is platform-specific but you can't tell which platform from the diff, ask the user
  instead of defaulting to `common`
- If the user says the version is already decided (e.g. "it will be 1.14.0"), use that version directly without asking
- After writing, do not modify any version entry other than the one being worked on
