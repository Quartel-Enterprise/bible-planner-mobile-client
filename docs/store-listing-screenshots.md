# Store listing screenshots

The screenshots on the Google Play and App Store listings are **generated, not captured**. Each
one renders a real screen of the app from hand-written sample data, inside a device frame drawn by
the [store-screenshots](https://github.com/lucianosantosdev/store-screenshots) library, with a title
and description in the listing's three locales (`en-US`, `pt-BR`, `es`). None of the listing images
is versioned: every one is rebuilt from source, locally or in CI. The same generators also produce
the [README's grid](#the-readme-grid), which *is* committed.

The two stores are rendered differently, because the app is not drawn the same way on each:

- **Google Play** — the screen renders under Robolectric, inside the frame. Any machine can do it.
- **App Store** — the screen renders on the **iOS simulator** first, so it shows what iOS really
  draws: SF Symbols instead of Material icons, the Calf components, the San Francisco font, and the
  iPad's own wide layout. Robolectric then puts that PNG into the iPhone or iPad frame, with the
  same banner. Robolectric can't do the first half itself: it runs the app's Android code, and SF
  Symbols only exist in UIKit. So the App Store shots **need a Mac with Xcode**.

The listings' words are versioned instead of generated — see
[Store listing metadata](store-listing-metadata.md).

## Regenerating locally

```bash
./gradlew collectPlayStoreScreenshots   # Play only, any machine
./gradlew collectAppStoreScreenshots    # App Store only, needs a Mac with Xcode
./gradlew collectStoreScreenshots       # both
```

Each one runs the six generators, **deletes its platform's folder in `store_listings/` and rewrites
it**, so a renamed or removed screen cannot leave a stale image behind. The folder is git-ignored;
regenerate as often as you like. The App Store run also builds the iOS test binaries, so its first
run on a machine takes a while. The layout is made for eyeballing:

```
store_listings/<store>/<language>/<device>/<screen>.png

android/{en,pt_br,es}/{phone,tablet_7,tablet_10}
ios/{en,pt_br,es}/{iphone_6_5,iphone_6_7,ipad_11,ipad_13}
```

Two variations:

- **One screen changed** and you want a fast look — run that module's generator alone, e.g.
  `./gradlew :feature:books:testAndroidHostTest` (about 20 seconds). It writes the Play shots to
  `build/outputs/store-screenshots/books/`; on its own it leaves the iPhone and iPad classes out
  (see [below](#how-the-app-store-half-works)). Mind that every generator first wipes the whole
  `build/outputs/store-screenshots/` directory, so a `collect*` straight after only collects that
  one module — for the full `store_listings/`, run one of the commands above.
- **See exactly what fastlane uploads** — `./gradlew stagePlayStoreScreenshots` writes the Play
  layout to `fastlane/metadata/android/` (with `es` fanned out to `es-419`/`es-ES`/`es-US`), and
  `./gradlew stageAppStoreScreenshots` writes the App Store one to `fastlane/screenshots/` (flat per
  locale with a device suffix, since `deliver` infers the device from the image size). These are
  what the fastlane lanes run.

The generators build the app for real, so they need the same local files a debug build does:
`local.properties` and `androidApp/google-services.json`.

## How the App Store half works

Each screen has three pieces, and the App Store shots go through all of them:

```
feature/<module>/src/
    commonTest/.../fixture/<Screen>ScreenshotContent.kt   # the screen with its sample data, shared
    iosTest/.../screenshots/<Screen>AppleScreenshotCaptures.kt   # renders it on the iOS simulator
    androidHostTest/.../screenshots/<Screen>Screenshots.kt       # frames it (Play: renders it too)
```

1. **The capture.** `<Screen>AppleScreenshotCaptures` calls `captureAppleScreenshots` from
   `:ui:testing`, which renders the shared content once per slot (`AppleScreenshotSlot`: the 6.5"
   and 6.7" iPhones, the 13" iPad) and per locale, at the slot's logical size and density, and
   writes a PNG to `build/outputs/apple-screenshot-captures/<module>/`. The locale is switched
   through `AppleLanguages`, the same way the app's language setting does it on iOS. A shot that
   needs a click first, like a tab of the study, does it in `beforeCapture`.
2. **The frame.** The iPhone and iPad classes of `<Screen>Screenshots` take an `appleSlot` and draw
   `AppleScreenshotImage`, the captured PNG, where the Play classes draw the screen. The iPad 11" class
   uses the 13" capture: the screen inside the frame is the same size, only the canvas is taller.
   The Apple classes use `edgeToEdge = true`: the capture fills the whole screen and leaves the
   status bar's height free at the top itself, so the frame's clock and icons sit on the app's
   background, as on a real device. That is why the light plan shot asks for dark status bar
   content.
3. **The wiring.** `configureAppleStoreScreenshots(screenshotsName = …)` from `build-logic`, called
   at the end of each module's `build.gradle.kts`, adds the `iosSimulatorArm64AppleScreenshotsTest`
   task, which runs only the `*AppleScreenshotCaptures` classes; the regular `iosSimulatorArm64Test`
   (and so the iOS job of `ui-tests`) leaves them out. It then decides from the tasks the build was
   asked for: a build that runs an `*AppStoreScreenshots` task or `collectStoreScreenshots` makes
   `testAndroidHostTest` depend on the captures and run the iPhone and iPad classes. Any other build
   leaves those classes out, so the Play listing, the README and a module's host tests never need
   a simulator.

Things that bite, on the iOS side:

- **Calf's native views don't show up.** The capture is Compose's own rendering, so a `UIKit` view
  that Calf puts on top (the native tab bar, an open menu, a native alert) is not in it. None of
  the listing's screens shows one today.
- **The capture sizes live in `AppleScreenshotSlot`.** They must match the frame's screen in the
  store-screenshots library, or the PNG is stretched to fit it. Check them after a library upgrade.

## What the images show

| # | Screen | Play | App Store | Notes |
|---|--------|------|-----------|-------|
| 01 | Reading plan (dark) | ✓ | ✓ | 1 Kings 10-12, week 16 — see below |
| 02 | Reader | ✓ | ✓ | Genesis 1:1-12 — the first-frame slot, see below |
| 03 | Day | ✓ | ✓ | Genesis 1-3 |
| 04 | Day study — summary | ✓ | ✓ | Genesis 1-3 |
| 05 | Books | ✓ | ✓ | |
| 06 | Reading plan (light) | ✓ | ✓ | The only light shot: "light or dark, it follows you" |
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

**The reader is second, and the order is the filename prefix.** Both stores show the shots in
filename order, so `01_`…`09_` *is* the shelf order — renaming a prefix reorders the listing. Only
the first two or three are visible without swiping, and the reader is the one shot that shows the
Bible text. It sits there because the listings spent years telling shoppers the app did not
include that text (see [Store listing metadata](store-listing-metadata.md)); the light plan, which
used to hold the slot, argued "light or dark" with a second picture of a screen already shown.
Moving a screen means changing its `fileName` and this table together.

**Play holds 8, the App Store 10.** Play caps a listing at eight screenshots per device type and
the App Store at ten per display size. The study's context tab (07) is the thinnest image, so it
only fills an App Store slot: `DayStudyScreenshots.dayStudyContext` skips the Play form factors
with `assumeTrue`.

## Where to edit

Each generator is split over three source sets of its feature module:

```
feature/<module>/src/
    commonTest/kotlin/.../fixture/
        <Screen>SampleData.kt          # the UiState the screen renders, hand-written per locale
        <Screen>ScreenshotContent.kt   # the screen as the shots show it, and the file names
    iosTest/kotlin/.../screenshots/
        <Screen>AppleScreenshotCaptures.kt   # the App Store captures on the iOS simulator
    androidHostTest/kotlin/.../screenshots/
        <Screen>Screenshots.kt         # banner copy per locale, background, one subclass per form factor
```

The sample data and the content sit in `commonTest` so that Robolectric and the simulator render
exactly the same screen; the Compose UI tests reuse the sample data too.

Generators: `reading_plan`, `day`, `day_study`, `books`, `read`, `chat`, and `book_details` for the
README only. Adding a module means one new entry in `storeScreenshotModules` (or
`readmeScreenshotModules`) in the root `build.gradle.kts` plus the same additions to the module's
`build.gradle.kts` the others have (`withHostTest`, the `androidHostTest` dependencies, `:ui:theme`
in `commonTest`, the `Test` task's output directory, and the `configureAppleStoreScreenshots` call).

Things that bite:

- **Top-level `private val`s must be packed** (a custom ktlint rule), and a documented declaration
  needs a blank line above it — so only the *first* val of the group can carry a KDoc. Put the
  group's explanation there.
- **Robolectric has no SDK 37.** The library pins the SDK; a hand-written Robolectric test in
  these source sets needs `@Config(sdk = [36])`.
- **Wide layouts are handed in by hand on Play.** The frame's content area on the tablet slots sits
  below the app's 840dp two-pane breakpoint, so screens that take an `isWide` flag get it
  explicitly per form factor (see `DayStudyScreenshots`); screens that measure it themselves render
  the narrow layout there. The iPad capture is 1024pt wide, so on the App Store those screens show
  their wide layout, as on a real iPad.
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

- **A production release refreshes both listings.** `release.yml` renders the iOS screenshots on a
  macOS job of their own (`stageAppStoreScreenshots`) while the IPA builds, and a second job
  uploads them (`fastlane ios upload_screenshots skip_render:true`) to the editable version
  **before** the build is submitted for review — a submitted version's screenshots are locked. It
  calls the `store screenshots` workflow for Play (`stagePlayStoreScreenshots`, on Linux) after the
  AAB is on the production track with a completed rollout. Every screenshot job is non-blocking:
  a failure ships the release with the previous images, visibly, and never holds the binary.
  Betas, test tracks and drafts skip both.
- **Rendering and uploading are separate jobs**, for both stores, with the images handed over as
  a build artifact (`app-store-screenshots`, `store-screenshots`). The render is the slow half and
  the store's API the one that fails, so retrying a failed upload sends the images that were
  already rendered. Run by hand without `skip_render`, both `upload_screenshots` lanes still
  render first. Either way they refuse to upload from an empty folder: both stores replace the
  listing's screenshots with what is staged, so that would wipe it.
- **Between releases**, the `store screenshots` workflow (Actions → *store screenshots* → Run
  workflow) republishes Play's listing on demand; App Store screenshots can only change with a new
  version. It defaults to `validate_only`, which regenerates and validates against the Play API
  without publishing, and uploads the rendered Play images as a `store-screenshots` artifact either
  way. To look at the App Store images between releases, run `collectAppStoreScreenshots` on a
  Mac.
- **Duplicates on App Store Connect are removed after every upload.** `deliver` re-uploads a
  screenshot when App Store Connect has not yet published its checksum, so a shelf can end up with
  the same file twice; the iOS lane walks the editable version afterwards and drops every later
  copy. `fastlane ios dedupe_screenshots` runs that pass on its own while a version is editable.

The Play service account needs the **Manage store presence** permission for listing uploads;
release permissions alone let AAB uploads through while screenshot validation fails with "the
caller does not have permission".
