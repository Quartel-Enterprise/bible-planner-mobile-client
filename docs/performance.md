# Performance

The Android app ships a Baseline Profile for its own code, and `:tools:baseline-profile` measures
the critical journeys with macrobenchmarks on a device. This page is where both become numbers, and
how to reproduce them.

## Baseline Profile

Libraries such as Compose, DataStore and RevenueCat ship profiles of their own inside their AARs,
and AGP merges them into the app. Until October 2026 that was all the app had: the merged profile
listed no class of the app itself, nor of Koin, Ktor, Supabase, kotlinx.serialization, Room or
Compose Resources. Every install and every update ran that code interpreted until the JIT, or the
Play Store's cloud profile days later, caught up.

`:androidApp` now ships two files, committed under
[`androidApp/src/main/generated/baselineProfiles`](../androidApp/src/main/generated/baselineProfiles):

- **`baseline-prof.txt`** lists the classes and methods the critical journeys run, the app's own
  code included. The install compiles them ahead of time. `profileinstaller` installs it on devices
  where the Play Store did not, including a sideloaded APK.
- **`startup-prof.txt`** lists what the cold start alone runs. R8 uses it to put those classes in
  the primary dex file (`dexLayoutOptimization`), so starting the app reads fewer pages.

Both come from [`BaselineProfileGenerator`](../tools/baseline-profile/src/main/kotlin/com/quare/bibleplanner/baselineprofile/BaselineProfileGenerator.kt),
which drives a release-like build (not minified, so the rules keep their real names, and not
debuggable) through these journeys with UiAutomator:

1. Cold start to the Plans tab (the only journey in the startup profile)
2. Fling the plan's weeks down and back up
3. Open a day and fling it
4. Open the day's first chapter and fling the text, which loads the next chapter on the way
5. Back to Plans, open the Books tab and fling it

The journeys live in [`Journeys.kt`](../tools/baseline-profile/src/main/kotlin/com/quare/bibleplanner/baselineprofile/Journeys.kt),
shared by the generator and the benchmarks. They find every screen by a test tag, never by text, so
they run on a device in any language. `MainActivity` sets `testTagsAsResourceId` above `AppRoot`, in
every build type, so UiAutomator sees the tags as resource ids. The tags are:

| Tag | Where |
|---|---|
| `plans_list`, `plan_day` | The plan's list and each of its day rows (`feature/reading_plan`) |
| `plans_tab`, `books_tab`, `profile_tab` | The bottom bar's tabs (`feature/main`) |
| `day_content`, `day_passages` | The day screen and its card of chapters (`feature/day`) |
| `read_chapters` | The reader's list of chapters, narrow and wide (`feature/read`) |
| `books_content` | The Books tab's content (`feature/books`) |

The benchmark module keeps its own copy of each, so the features' UI tests and
`MainScreenViewModelTest` assert the same strings: renaming a tag fails those tests on CI instead of
a journey on a device.

## Measured

Each journey is measured twice: with `CompilationMode.None()`, which drops every compiled method and
runs the app with no profile at all, and with `CompilationMode.Partial(BaselineProfileMode.Require)`,
which installs the shipped profile (the libraries' and the app's) first. Ten iterations each.

**Device:** a Samsung Galaxy A56 (SM-A566E, Exynos 1580, 8 cores) on Android 16, clocks not locked,
signed in and with a Bible version downloaded. Measured on 2026-10-04.

### Cold start

`StartupTimingMetric`, time to initial display, from the launcher to the first frame.

| | Without profile | With profile | Change |
|---|--:|--:|--:|
| Min | 643.4 ms | 468.2 ms | −27.2% |
| **Median** | **655.4 ms** | **485.6 ms** | **−25.9%** |
| Max | 672.4 ms | 513.6 ms | −23.6% |

### Frames

`FrameTimingMetric`. `frameDurationCpuMs` is the CPU time a frame took; `frameOverrunMs` is how late
it was for its deadline, so anything above zero is a frame the user saw drop.

**Plans** (cold start, then three flings down and three back up):

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 5.3 / 10.4 / 20.2 ms | 4.9 / 8.0 / 14.5 ms |
| Overrun P50 / P90 / P99 | 0.8 / 6.4 / 19.6 ms | −0.4 / 3.9 / 11.1 ms |

**Books** (cold start, open the tab, fling it):

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 5.8 / 14.1 / 33.9 ms | 5.6 / 9.3 / 20.5 ms |
| Overrun P50 / P90 / P99 | 2.9 / 11.8 / 58.6 ms | 2.3 / 5.7 / 29.1 ms |

**A day** (cold start with a whole day row on screen, then open it and fling it):

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 5.0 / 9.1 / 21.3 ms | 4.7 / 7.1 / 14.9 ms |
| Overrun P50 / P90 / P99 | 0.0 / 4.8 / 27.5 ms | −0.6 / 3.9 / 13.2 ms |

**A chapter** (from the day, open its first chapter and fling the text):

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 4.4 / 7.6 / 17.5 ms | 4.2 / 6.4 / 14.4 ms |
| Overrun P50 / P90 / P99 | −1.7 / 3.5 / 22.6 ms | −2.0 / 3.1 / 13.9 ms |

The median frame barely moves: it is already cheap. The slow ones do, because without a profile the
first time each screen draws runs interpreted code while the JIT catches up. The worst Books frames
are late by half of what they were, and the first trip into a day or a chapter by about half too.

### What the app's profile adds

`CompilationMode.None()` is not what a user gets: even before this profile, the libraries' profiles
were installed with the app. To see what the app's own profile adds over those, the release build
from before it and the one with it were measured on the same device, each installed the way a user
gets it (`profileinstaller` writes the shipped profile, then `cmd package compile -m speed-profile`),
with `adb` instead of Macrobenchmark. Two rounds each.

| | Libraries' profiles only | With the app's profile |
|---|--:|--:|
| Cold start (`am start -W`, 20 launches), median | 348 / 349 ms | 306 / 308 ms |
| Cold start, P90 | 364 / 365 ms | 314 / 317 ms |
| Plans flings after a cold start, janky frames (`gfxinfo`) | 1.6% / 1.6% | 1.5% / 1.5% |
| A day opened and flung after a cold start, janky frames | 5.0% / 4.5% | 4.9% / 5.0% |
| Same, P99 frame | 39 / 31 ms | 31 / 27 ms |

The cold start is where it pays: 12% faster at the median, more at the tail. Compiling the whole app
ahead of time (`cmd package compile -m speed`) only reached 355 ms on the same device, so the gain
comes from the startup profile's dex layout as much as from compiled code. The frames were already
as good as the libraries' profiles make them on this device: their hot path is Compose, which was
covered. A slower device has more to gain there.

## Reproducing

Both need a **physical phone in portrait** on API 28 or newer, with network access, signed in to an
account whose plan has started and with a Bible version downloaded, so a day and a chapter have
something to show. Portrait, because the journeys go through the bottom bar and the one-pane day: a
tablet, an unfolded foldable or a phone on its side gets the wide layout, and the journeys stop
right after the cold start with a message saying so. An emulator's frame times are noise: the same build swung between 14% and 29% janky frames
from one run to the next. Neither runs on CI (see [CI](ci.md#ui-tests)).

The run installs the benchmark build over the app already on the device, signed with the same debug
key, so the device's data stays. Gradle uninstalls the apps it installed when the run ends, and
with them that data; `leaveApksInstalledAfterRun` keeps them. `ANDROID_SERIAL` keeps the run off
every other device and emulator adb sees.

The journeys only open screens and scroll: they never tick a chapter or a day as read. The builds
are release builds, though, so they report analytics and crashes to production like any other. Only
`release` uploads its R8 mapping file to Crashlytics: `nonMinifiedRelease` and `benchmarkRelease`,
which the Baseline Profile plugin derives from it, never do.

**Regenerate the profile** after a change to a journey or to the code it runs, and commit what it
writes. The two files run to tens of thousands of lines; `.gitattributes` marks them generated, so
a pull request's diff collapses them. It takes about 40 minutes:

```bash
ANDROID_SERIAL=<device> ./gradlew :androidApp:generateBaselineProfile -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
```

**Run the benchmarks**, about an hour for all of them:

```bash
ANDROID_SERIAL=<device> ./gradlew :tools:baseline-profile:connectedBenchmarkReleaseAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
```

The results land in
`tools/baseline-profile/build/outputs/connected_android_test_additional_output/benchmarkRelease`,
as JSON plus a Perfetto trace per iteration. A single class runs with
`-Pandroid.testInstrumentationRunnerArguments.class=com.quare.bibleplanner.baselineprofile.StartupBenchmark`.

What each benchmark does to reach its screen:

- `StartupBenchmark` uses `StartupMode.COLD`: the app is killed before each iteration.
- `ScrollBenchmark` and `ReadingBenchmark` kill the app themselves at the start of each setup, so
  every iteration draws its screen for the first time. `StartupMode.COLD` would kill it after the
  setup, and the measured block would find nothing on screen.
- `ReadingBenchmark` brings a whole day row on screen in the setup, so the Plans scrolling that may
  take is not counted as the day's frames.
- The bottom bar exits when Plans scrolls down, so the journeys scroll the list back up until the
  tab they need is on screen.
