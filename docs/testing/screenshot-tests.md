# Screenshot tests

The main screens are rendered under Robolectric and compared, pixel by pixel, against images
committed to the repository. A layout that shifts, a colour that changes, a string that starts to
truncate: the check fails, and the difference is an image a reviewer can look at. Every render also
runs Google's [Accessibility Test Framework](https://github.com/google/Accessibility-Test-Framework-for-Android),
the checks behind Accessibility Scanner.

They complement the [Compose UI tests](compose-ui-tests.md) rather than replace them: a UI test
asserts what a screen shows and the events it sends, and a screenshot test catches how it looks.
They only see the Android rendering: SF Symbols, the Calf components and the iOS insets are
covered by the UI tests on the iOS simulator, not here.

The setup follows TuneScout's, adapted to feature isolation: no module may depend on every
feature, so each feature keeps its screenshot tests in its own `androidHostTest`, next to the
[store screenshot generators](../store-listing-screenshots.md) that already render there.

## Running them

```bash
./gradlew screenshotTests -PscreenshotTests=verify
```

Verifying on macOS **fails**: the references are rendered on Linux, and the same screen comes out a
few pixels different on another platform. Locally the useful run is the plain one, which renders
every screen and runs the accessibility checks without comparing anything:

```bash
./gradlew :feature:books:testAndroidHostTest --tests '*ScreenshotTest'
```

`-PscreenshotTests=record` writes the references into `src/androidHostTest/screenshots`, so it is
how a render can be looked at locally. Don't commit those. A new test's images are untracked, so
`git checkout` leaves them behind; discard both kinds before committing:

```bash
git checkout HEAD -- ':(glob)**/src/androidHostTest/screenshots/**'
git clean -fd -- ':(glob)**/src/androidHostTest/screenshots/**'
```

The first fails when no reference is committed yet; the second is then enough.

CI verifies on every pull request (the `screenshot-tests` workflow, see
[docs/ci.md](../ci.md#screenshot-tests)), a pull request labeled `record-screenshots` included, and uploads `screenshot-differences` when it fails: one
`<name>_compare.png` per failure, holding the reference, the new render and the difference between
them side by side.

## Changing a screen on purpose

The references are part of the change, but they can't be recorded on a developer's machine. Label
the pull request **`record-screenshots`**: the `record-screenshots` workflow renders them on Linux,
commits them to the branch and drops the label, and the commit it pushes is verified like any
other. The verification of the commit before it fails on the outdated references, which is
expected. A run that fails keeps the label on, and adding it again retries. `create-pr` adds the
label when the branch changes how a covered screen looks.

Update the branch with `main` before asking for the recording. A pull request is verified against
its **merge** with `main`, while the references are recorded on the branch alone: a screen `main`
changed in the meantime would be recorded as the branch still draws it and fail verification right
after.

## Adding a test

One class per screen named `<Screen>ScreenshotTest`, extending `ScreenshotTest` from
`:ui:screenshots:testing`, with one `@Test` per state worth looking at:

```kotlin
internal class BooksScreenshotTest : ScreenshotTest() {
    @Test
    fun grid() {
        snapshot(
            name = "grid",
            variants = ScreenshotVariant.all,
        ) {
            BooksContent(uiState = booksUiState().copy(layoutFormat = BookLayoutFormat.Grid))
        }
    }
}
```

`snapshot` renders the composable once per [variant](#variants) into
`src/androidHostTest/screenshots/<test class>/<name>_<variant>.png`, which is the file a reviewer
opens. It takes the screen's stateless composable and a `UiState`, the same pair the UI tests use,
and the same fixtures in `commonTest`. The base wraps it in `AppTheme` with the variant's theme.

A test is one `snapshot` call and has no Given / When / Then sections: the state is the given, and
the image is the then. Test names are plain camelCase, since they are only read next to the image
names.

A module gets screenshot tests by calling `configureScreenshotTests()` at the end of its build
script. It adds `:ui:screenshots:testing` to the module's `androidHostTest` and registers the
`screenshotTests` task. The module's own `withHostTest {}` must set
`isIncludeAndroidResources = true`, or its strings would render empty; AGP accepts only one
`withHostTest` per module, so the convention can't set it, but the build fails with a message
naming the module when it is missing.

### Variants

| Variant | What it renders |
| --- | --- |
| `LIGHT`, `DARK` | the default for every snapshot, since most regressions are a colour that reads in one theme and not the other |
| `LARGE_FONT_PT_BR` | Portuguese copy (longer than English) at a 1.5x font scale, the combination that truncates or wraps first |
| `LARGEST_FONT` | English at a 2x font scale, the largest the system's font size setting reaches, where a fixed height clips and a row stops fitting its controls |

The last two are deliberately not the default: passing `variants = ScreenshotVariant.all` on the
states that carry the most text says where truncation matters, instead of multiplying every image in
the repository. `isLandscape = true` covers the layouts that switch on width.

### What is covered

| Test | States |
| --- | --- |
| `BooksScreenshotTest` | loading, list, grid, filter menu, searching |
| `DayScreenshotTest` | loading, loaded, loaded in landscape, read, without notes |
| `ReadingPlanScreenshotTest` | loading, on track, on track in landscape, behind |
| `ProfileScreenshotTest` | logged out, logged in |

Left out on purpose: the day study card, which the day screen gets from the root and
`:feature:day_study` would cover, and the reading plan's dropdown menu, whose popup Roborazzi does not
capture over the screen it opens on.

## Accessibility checks

Every capture is handed to the Accessibility Test Framework through Roborazzi's
`checkRoboAccessibility`: touch targets under 48dp, text and image contrast, items with no label,
clickable items that share one. It runs on each root, so a dialog is checked as well as the screen
under it.

An **error** fails the test. **Warnings** are printed and don't: they include measurements no screen
can settle, like contrast read off a shimmer placeholder. Read them in the test's standard output
when touching colours.

This needs no reference image, so unlike the comparison it also fails locally: the plain run above
is enough to see an accessibility error before pushing.

## The pieces that are not obvious

**The mode is a property of its own.** The store screenshot generators share the module's host
test task and capture through Roborazzi too, with `roborazzi.test.record` on for the whole task.
`ScreenshotTest` hands Roborazzi an explicit task type read from `screenshotTests.mode`, which only
`-PscreenshotTests` sets, so the two never see each other's mode. With the property set, the task
runs the `*ScreenshotTest` classes alone; without it they run with the rest of the host tests,
except in a build asked for the store or README screenshots (any requested task ending in
`Screenshots`), which only wants the generators.

**A capture owns its Compose rule.** `ScreenshotTest` creates a `createComposeRule()` per image and
evaluates it by hand. The rule holds the single `setContent`, and it is also what keeps endless
animations (the shimmer, the progress indicators) from spinning frames forever instead of settling
into a frame to photograph.

**The device is set again for every image.** Robolectric keeps the orientation of the previous
configuration, and swaps a portrait size to match it, so a portrait capture after a landscape one
came out landscape. Each capture sets the Pixel 7, the language and the orientation from scratch.

**The images are half size.** Roborazzi's `resizeScale` halves the 1078x2399 the Pixel 7 renders,
through the capture's own options rather than the global system property, which would also halve
the store screenshots. A layout, a colour or a truncated string is as visible at half the width, in
a quarter of the bytes, and these files are versioned.

**Robolectric runs on SDK 35**, the one the store generators already use, so both share one
Android runtime download and neither needs extra JDK flags.

**A record run wipes the module's references first**, so a renamed or removed state leaves no
orphan image behind. Recording with `--tests` therefore deletes the other classes' images.
