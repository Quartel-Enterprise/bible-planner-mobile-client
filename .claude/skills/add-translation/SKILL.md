---
name: add-translation
description: "Add a new language to the app: register it in the translations check, translate every module's composeResources strings.xml and the release notes, wire it into the Language enum and the platform locale code, verify the build and update the docs."
---

# Add Translation

This skill adds a whole new language to Bible Planner. English in
`src/commonMain/composeResources/values/strings.xml` is the source of truth; every other language
lives in a `values-<qualifier>/strings.xml` next to it, in every module that has strings.

The `translations` workflow runs [`scripts/check_translations.py`](../../../scripts/check_translations.py)
on every pull request that touches a `strings.xml`. It fails when a locale listed in its
`REQUIRED_LOCALES` is missing a file, is missing a key, or declares a key the English file doesn't.
Adding the new locale there is what turns "a translation" into "a supported language": from then on,
nobody can add an English string without translating it into this language too.

Strings are only half of it here: the app has its own language picker, reading content and store
listings keyed by language, so the language also has to be wired into code (step 5).

## Step-by-step workflow

### 1. Pick the resource qualifier

Ask the user which language only if the request doesn't name it. Then choose the folder qualifier:

- **Language only** (`values-es`, `values-fr`, `values-de`) by default. It covers every region of
  that language — `values-es` serves Spain, Mexico and Argentina alike — and every platform falls
  back to it from any regional locale.
- **Language + region** (`values-pt-rBR`, `values-es-rMX`) only when the copy really differs by
  region *and* the user asked for that specific variant. Brazilian Portuguese is regional because
  European Portuguese differs enough to matter.

Check the qualifier isn't already present:

```bash
find . -type d -name 'values-<qualifier>' -path '*composeResources*' -not -path '*/build/*' -not -path './.claude/*'
```

### 2. Register the locale in the check first

Add the folder name to `REQUIRED_LOCALES` in `scripts/check_translations.py`:

```python
REQUIRED_LOCALES = ('values-pt-rBR', 'values-es', 'values-<qualifier>')
```

Then run the check before translating anything:

```bash
python3 scripts/check_translations.py
```

It must fail with one `file is missing (N untranslated strings)` line per module. That list is the
work to do, and it proves the check sees the new locale — if it passes here, the qualifier is wrong.

### 3. Translate every module

There are dozens of modules, so split the list from step 2 across parallel subagents (a handful of
modules each), handing each one the rules below. For each file the check reported, read the English
`values/strings.xml` **and** the existing `values-pt-rBR/strings.xml` and `values-es/strings.xml`
next to it (they show which strings are casual, which are button labels and which are content
descriptions), then write `values-<qualifier>/strings.xml`.

Rules:

- **Same keys, same order** as the English file. Keep the XML header and `<resources>` root.
- **Skip `translatable="false"` entries entirely** — don't copy them.
- **Keep the brand**: `Bible Planner` is never translated.
- **Bible terms follow the language's usual Bible tradition**: book names, "Old/New Testament",
  "chapter", "verse" as a Bible reader in that language knows them (look at how the existing
  Portuguese and Spanish files handle them).
- **Keep format arguments exactly**: `%1$s`, `%1$d` stay, with the same index and type. Reorder
  them inside the sentence if the grammar needs to, but never drop one.
- **Write apostrophes literally** (`doesn't`, `l'app`). Never escape them as `\'`: Compose Resources
  renders the backslash. Quotes too are written literally; `&` needs `&amp;` and `<` needs `&lt;`.
- **Plurals**: translate every `<plurals>` and give each quantity the language needs. Two
  (`one`, `other`) cover English, Portuguese and Spanish; languages like Russian or Polish also need
  `few`/`many`. Look up the language's CLDR plural rules when unsure.
- **Tone**: warm, informal and direct, addressing the user as "you" (Spanish *tú*, French *tu*,
  Portuguese *você*), short sentences, the same as the English copy.
- **Terms stay consistent across modules**: the same English word (Plan, Reading, Book, Chapter)
  gets the same translation everywhere — shared components and feature modules repeat labels.
- **Content descriptions** are read by TalkBack and VoiceOver: translate them as spoken phrases, not
  as abbreviations.

### 4. Translate the release notes

The release notes shown in the app and in the stores are JSON per language in
`feature/release_notes/src/commonMain/composeResources/files/release_notes/`. Create
`<language>.json` with every version, bucket and item of `en.json`, translated with the tone rules of
the `release-notes-updater` skill, and add the language code to `LANGUAGES` in
`scripts/check_release_notes.py`. Then:

```bash
python3 scripts/check_release_notes.py
```

`fastlane/Fastfile` maps each release notes language to its store locales. Add the new language
there only if the app's store listings get that locale (step 7).

### 5. Wire the language into the code

Add the value to the `Language` enum in `core/utils/.../locale/Language.kt`, then compile:

```bash
./gradlew compileKotlinJvm
```

Every exhaustive `when` over `Language` fails until it handles the new value — the language
provider of each platform, the app-language picker and its analytics, the Bible version, Day Study
and release notes language codes, the store and support links. Fix each one with the value that
language really needs (don't copy the English branch blindly: a missing Bible version or store link
for that language is a question for the user). Then find the places the compiler can't see:

```bash
grep -rn "SPANISH\|\"es\"" --include='*.kt' --exclude-dir=build --exclude-dir=.claude .
```

The iOS project also has to know the region: add the qualifier (`pt-BR` style, not `pt-rBR`) to
`knownRegions` in `iosApp/iosApp.xcodeproj/project.pbxproj`.

### 6. Verify

Run the check again — it must now pass and list the new locale:

```bash
python3 scripts/check_translations.py
```

Then build, so a malformed XML or a bad plural fails here instead of in CI, and run the tests
touched by step 5:

```bash
./gradlew :androidApp:assembleDebug jvmTest
```

Optionally see it on a device by picking the language in the app's language setting.

### 7. Store listings and docs

The store listings (`fastlane/metadata`, see [`docs/store-listing-metadata.md`](../../../docs/store-listing-metadata.md)
and [`docs/store-listing-screenshots.md`](../../../docs/store-listing-screenshots.md)) are a
separate decision: ask the user whether the new language should get its own listing now. Don't
create store locales unasked.

Then update every doc that lists the supported languages:

```bash
grep -rn "pt-BR\|pt-rBR\|Brazilian Portuguese\|Spanish" --include='*.md' --include='*.py' --exclude-dir=build --exclude-dir=.claude .
```

including the module docstring at the top of `scripts/check_translations.py`.

### 8. Wrap up

Summarize for the user: the qualifier used, how many modules were translated, the check results,
every code branch where you had to pick a value in step 5, and any term you had to choose between
(so a native speaker can review it). Then offer the `create-pr` skill — the branch type is
`feature`, since users get a new language.

## Adding strings later

Once a language is in `REQUIRED_LOCALES`, every new English string needs its translation in the
same pull request — the `translations` workflow fails otherwise. When the check reports
`missing translation for: <keys>`, add those keys to each listed file following the rules in step 3.
When it reports `not declared in values/strings.xml`, the key was renamed or removed in English:
rename or remove it in the translation too.
