# Store listing screenshots

The screenshots on the Google Play and App Store listings are **generated, not captured**. Each
one renders a real screen of the app under Robolectric, inside a device frame drawn by the
[store-screenshots](https://github.com/lucianosantosdev/store-screenshots) library, with a title
and description in the listing's three locales (`en-US`, `pt-BR`, `es`). None of the listing images
is versioned: every one is rebuilt from source, locally or in CI. The same generators also produce
the [README's grid](#the-readme-grid), which *is* committed.

## Regenerating locally

```bash
./gradlew collectStoreScreenshots
```

Runs the six generators (about two minutes), **deletes `store_listings/` and rewrites it**, so a
renamed or removed screen cannot leave a stale image behind. The folder is git-ignored; regenerate
as often as you like. The layout is made for eyeballing:

```
store_listings/<store>/<language>/<device>/<screen>.png

android/{en,pt_br,es}/{phone,tablet_7,tablet_10}
ios/{en,pt_br,es}/{iphone_6_5,iphone_6_7,ipad_11,ipad_13}
```

Two variations:

- **One screen changed** and you want a fast look — run that module's generator alone, e.g.
  `./gradlew :feature:books:testAndroidHostTest` (about 20 seconds). It writes to
  `build/outputs/store-screenshots/books/`. Mind that every generator first wipes the whole
  `build/outputs/store-screenshots/` directory, so a `collectStoreScreenshots` straight after only
  collects that one module — for the full `store_listings/`, run the first command.
- **See exactly what fastlane uploads** — `./gradlew stageStoreScreenshots` writes the store
  layouts to `fastlane/metadata/android/` (Play, with `es` fanned out to `es-419`/`es-ES`/`es-US`)
  and `fastlane/screenshots/` (App Store, flat per locale with a device suffix, since `deliver`
  infers the device from the image size). This is what CI runs.

The generators build the app for real, so they need the same local files a debug build does:
`local.properties` and `androidApp/google-services.json`.

## What the images show

| # | Screen | Play | App Store | Notes |
|---|--------|------|-----------|-------|
| 01 | Reading plan (dark) | ✓ | ✓ | 1 Kings 10-12, week 16 — see below |
| 02 | Reading plan (light) | ✓ | ✓ | The only light shot: "light or dark, it follows you" |
| 03 | Day | ✓ | ✓ | Genesis 1-3 |
| 04 | Day study — summary | ✓ | ✓ | Genesis 1-3 |
| 05 | Books | ✓ | ✓ | |
| 06 | Reader | ✓ | ✓ | Genesis 1:1-12 |
| 07 | Day study — context | | ✓ | App Store only |
| 08 | Day study — questions | ✓ | ✓ | |
| 09 | AI chat | ✓ | ✓ | One exchange about Genesis 1-3 |

Every shot renders the app in **dark theme** on the logo's blue dimmed to `#141C3D`, so the app's
own accents stay the brightest thing in each image; the full-strength `#4A6CF7` is the same blue
the accents use and out-glows them.

**Genesis, but the plan at week 16.** The day, reader, study and chat fixtures open on Genesis 1-3
because that is the passage a shopper recognises from a thumbnail. The plan screenshot
deliberately stands at week 16 of 1 Kings instead: Genesis is day 1 of the plan, where the
progress card has nothing to show, and that card is the argument for a plan that keeps its place.
The fixtures say so in their comments — do not "fix" the divergence.

**Play holds 8, the App Store 10.** Play caps a listing at eight screenshots per device type and
the App Store at ten per display size. The study's context tab (07) is the thinnest image, so it
only fills an App Store slot: `DayStudyScreenshots.dayStudyContext` skips the Play form factors
with `assumeTrue`.

## Where to edit

Each generator lives in its feature module's `androidHostTest` source set:

```
feature/<module>/src/androidHostTest/kotlin/.../screenshots/
    <Screen>Screenshots.kt   # banner copy per locale, background, one subclass per form factor
    <Screen>SampleData.kt    # the UiState the screen renders, hand-written per locale
```

Generators: `reading_plan`, `day`, `day_study`, `books`, `read`, `chat`, and `book_details` for the
README only. Adding a module means one new entry in `storeScreenshotModules` (or
`readmeScreenshotModules`) in the root `build.gradle.kts` plus the same three additions
to the module's `build.gradle.kts` the others have (`withHostTest`, the `androidHostTest`
dependencies, the `Test` task's output directory).

Things that bite:

- **Top-level `private val`s must be packed** (a custom ktlint rule), and a documented declaration
  needs a blank line above it — so only the *first* val of the group can carry a KDoc. Put the
  group's explanation there.
- **Robolectric has no SDK 37.** The library pins the SDK; a hand-written Robolectric test in
  these source sets needs `@Config(sdk = [36])`.
- **Wide layouts are handed in by hand.** The frame's content area on the tablet and iPad slots
  sits below the app's 840dp two-pane breakpoint, so screens that take an `isWide` flag get it
  explicitly per form factor (see `DayStudyScreenshots`); screens that measure it themselves render
  the narrow layout there.
- **Apple slots render at the slot's logical size** since store-screenshots 1.5.8 (428×926,
  430×932, 1024×1366). On 1.5.7 and earlier the Apple frames measured content at the bezel's
  on-canvas footprint (~348dp on the 6.5" slot), which drew everything ~20% too large.

## The README grid

The [README](../README.md)'s ten images come out of the same generators, from the same fixtures, as
a second variant:

```bash
./gradlew updateReadmeScreenshots
```

It rewrites `docs/screenshots/`, which — unlike `store_listings/` — **is versioned**: a reader has
to see the app without running anything. Committing the result is part of the change that moved the
screen.

What the variant does differently:

- **No banner copy.** `FramedLayout` guards the title and description on `isNotEmpty()`, so the
  `screenshot()` call simply passes neither and the device takes the whole canvas.
- **English only.** The file is committed once and read in one language, so there is no locale loop.
- **One form factor.** The phone frame for the grid; a landscape 10" tablet, through
  `ScreenshotStyle.mockupFrame` with `MockupOrientation.Landscape`, for the one wide shot.
- **Written small.** The task scales each PNG down before writing it — 460px wide for the phone
  shots, 1200px for the landscape one — in halving steps, since a single draw from 1242px leaves
  hairlines ragged. All ten weigh about 750 KB.

The classes are named `Readme*Screenshots` and sit in the same file as the listing generators for
that screen. Two of the screens have no listing counterpart — the book details, and the chapter with
its verses highlighted:

| File | Generator | Note |
|---|---|---|
| `plan.png`, `plan_light.png` | `reading_plan` | |
| `day.png` | `day` | |
| `study.png` | `day_study` | The summary tab only. |
| `books.png` | `books` | |
| `book.png`, `wide_book.png` | `book_details` | README-only module: Psalms, half read, synopsis open — 150 chapters is what fills the landscape shot's column. |
| `reader.png`, `highlights.png` | `read` | `highlights` is the same chapter with `areVersesHighlighted`. |
| `chat.png` | `chat` | |

`:feature:book_details` is therefore in `readmeScreenshotModules` but not in `storeScreenshotModules`
in the root `build.gradle.kts`; every other generator feeds both.

## How they reach the stores

- **A production release refreshes both listings.** `release.yml` uploads the iOS screenshots to
  the editable version **before** submitting the build for review — a submitted version's
  screenshots are locked — and calls the `store screenshots` workflow for Play after the AAB is on
  the production track with a completed rollout. Every screenshot step is non-blocking: a failure
  ships the release with the previous images, visibly, and never holds the binary. Betas, test
  tracks and drafts skip both.
- **Between releases**, the `store screenshots` workflow (Actions → *store screenshots* → Run
  workflow) republishes Play's listing on demand; App Store screenshots can only change with a new
  version. It defaults to `validate_only`, which regenerates and validates against the Play API
  without publishing, and uploads the rendered images as a `store-screenshots` artifact either
  way.
- **Duplicates on App Store Connect are removed after every upload.** `deliver` re-uploads a
  screenshot when App Store Connect has not yet published its checksum, so a shelf can end up with
  the same file twice; the iOS lane walks the editable version afterwards and drops every later
  copy. `fastlane ios dedupe_screenshots` runs that pass on its own while a version is editable.

The Play service account needs the **Manage store presence** permission for listing uploads;
release permissions alone let AAB uploads through while screenshot validation fails with "the
caller does not have permission".
