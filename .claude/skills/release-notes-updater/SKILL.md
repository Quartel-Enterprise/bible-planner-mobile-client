---
name: release-notes-updater
description: "Update the pt.json, en.json, and es.json release notes files with user-friendly descriptions of what changed in the new app version."
---

# Release Notes Updater

This skill updates the three release notes JSON files (`pt.json`, `en.json`, `es.json`) that are
displayed directly to users inside the app. The notes need to be clear and friendly — avoid jargon
and write as if explaining to someone who uses the app but knows nothing about software development.

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
- Values are arrays of user-facing strings describing what changed

```json
{
  "1.13.0": [
    "Fix extra whitespace below the search bar on the books screen."
  ],
  "1.12.0": [
    "Notifications about the download progress of Bible versions."
  ]
}
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
Use that version. If its notes array is empty or the user wants to add more items, proceed to step 4.

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

# Committed changes not yet in origin/develop
git log origin/develop..HEAD --oneline

# Uncommitted changes (staged and unstaged)
git diff HEAD
```

Use the commit messages and diff output to figure out what changed from a user's perspective.
If you can extract meaningful changes from this output, proceed directly to step 5 — do not ask the user.

Only ask the user if:
- Both commands return nothing (no commits ahead of origin/develop and no uncommitted changes), or
- The output is too ambiguous to determine what actually changed for the user

In that case, ask:
> "What changed in this version? Describe it simply, as you would explain it to someone who just uses the app."

If the user already described the changes earlier in the conversation, use that — don't run the commands or ask again.

### 5. Write user-friendly notes in three languages

Transform the user's description into polished, user-facing sentences. The golden rules:

- **No jargon**: say "search bar" not "SearchBar component"; say "books screen" not "BooksScreen"; say "fixed a problem" not "bug fix" or "null pointer exception"
- **Short and clear**: one sentence per change, starting with an action verb
- **User's perspective**: describe what they *experience*, not what changed in the code
- **Positive framing**: "You can now..." or "Fixed an issue that..." instead of dry technical descriptions
- **Omit internal changes**: refactors, architecture changes, and dependency updates that have no visible user impact should be skipped entirely

Write the notes in all three languages:
- **pt** (Portuguese — Brazil)
- **en** (English)
- **es** (Spanish — Latin America)

Read a few existing entries in each file to calibrate the style and tone before writing.

### 6. Update the JSON files

- Place the version entry at the **top** of each JSON file (versions are ordered newest → oldest)
- If the version slot already existed with some notes, **append** the new items to the existing array
- Edit all three files: `pt.json`, `en.json`, `es.json`
- Confirm briefly to the user what was added and in which version

## Example transformations

| Developer says (raw) | Written in the JSON |
|---|---|
| "removed extra whitespace below the search bar on the books screen" | "Fixed extra whitespace that appeared below the search bar on the books screen." |
| "added download progress notifications for bible versions" | "You now receive notifications about the download progress of Bible versions." |
| "refactored MoreScreen for better usability" | "The 'More' screen has been reorganized to be easier to use." |
| "fix null pointer on offline mode" | "Fixed an issue that could crash the app when used without an internet connection." |
| "migrated ViewModel from LiveData to StateFlow" | *(omit — internal change with no visible user impact)* |

## Edge cases

- If the user provides notes for multiple changes at once, create one array item per change
- If a version slot exists in `en.json` but not in `pt.json` or `es.json`, create it in the missing files too
- If the user says the version is already decided (e.g. "it will be 1.14.0"), use that version directly without asking
- After writing, do not modify any version entry other than the one being worked on
