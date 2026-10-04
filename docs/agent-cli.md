# Agent CLI

`:tools:agent-cli` runs the app without a window, simulator or emulator. It starts the real Koin
graph, Room database, DataStore and ViewModels on the JVM, keeps a back stack the way
`RootAppNavDisplay` does, and takes commands: open a route, send a `UiEvent`, read every flow a
ViewModel exposes as JSON, and see what each step navigated to, emitted and tracked.

It exists so a coding agent can check a change to a ViewModel, use case or repository in the
running app in milliseconds per step, instead of driving a simulator through screenshots. It does
not replace the UI tests: nothing is drawn, so layout, insets, animations and anything a composable
decides on its own are out of its reach.

## Quick start

```bash
scripts/agent-cli.sh start
```

The first start builds the module and takes a few seconds. The session then stays warm in the
background, and every command is a loopback HTTP call:

```bash
scripts/agent-cli.sh event OnDayClick '{"dayNumber": 1, "weekNumber": 1}'
```

```bash
scripts/agent-cli.sh state DayViewModel.uiState.completedPassagesCount
```

```bash
scripts/agent-cli.sh stop
```

`scripts/agent-cli.sh -` sends every line of stdin, one command per line, and answers with a JSON
array. `scripts/agent-cli.sh repl` runs a session on stdin instead of in the background, which is
handy for a script piped in once. Lines starting with `#` are skipped in both.

Options for `start` and `repl`:

| Option | What it does |
|---|---|
| `--fresh` | Deletes the data directory first: a first launch, signed out, with nothing downloaded. It refuses a directory the CLI didn't create. |
| `--wide` | Behaves like a wide window: the study opens beside the day or the chapter, both panes' ViewModels share the screen, and every screen is told it is wide. |
| `--settle-ms <ms>` | How long nothing may change before a command answers (default 300). |
| `--compact` | One line of JSON per response. |

## Commands

| Command | What it does |
|---|---|
| `routes [filter]` | Every route, its arguments and the ViewModels it shows. |
| `open <Route> [{json}]` | Navigates to a route, as `Navigator.navigate` does. |
| `replace <Route> [{json}]` | Navigates replacing the top screen. |
| `back` | Navigates back. |
| `reset` | Goes back to a single main screen. |
| `stack` | The back stack, with each route's arguments. |
| `state [path] [--limit n] [--app]` | Every flow of the top screen's ViewModels, or of the app-level ones with `--app`. |
| `events [ViewModel]` | The `UiEvent`s the top screen accepts, with their arguments. |
| `event [ViewModel.]Name [{json}]` | Builds an event and sends it to `onEvent`. |
| `functions [ViewModel]` | The public functions of the top screen's ViewModels. |
| `call ViewModel.function [{json}]` | Calls a public function with named arguments and answers its result. |
| `wait <path>[ = json] [--timeout ms] [--app]` | Waits until a state path exists, or equals a value (a bare word is text). |
| `settle [ms]` | Waits until nothing changed for that long. |
| `log` | Answers nothing but the log since the last command. |
| `help`, `quit` | |

A path walks the JSON of `state`: `ReadingPlanViewModel.uiState.weekPlans[0].dayPlans[2].isOverdue`.
Long lists stop after 50 items, ending in `"…more n"`; `--limit 0` shows everything. Event arguments
are read with kotlinx.serialization first and through the constructor otherwise, so a sealed
argument takes `"@type"`, the same field `state` writes.

## Responses

Every command answers one JSON object:

```json
{"ok": true, "result": {...}, "log": [...], "ms": 312}
```

- `result` is what the command asked for. A command that can change the screen (`open`, `back`,
  `event`, `call`, `reset`) answers once nothing has changed for the settle time, with the screen it
  ended on; `"settled": false` means something was still changing after 10 seconds.
- `log` is everything that happened since the previous command, in order: `navigation`, `action`
  (anything a ViewModel emitted through a `SharedFlow`, such as its `UiAction`), `snackbar`,
  `analytics` (every event and user property the app would have sent to GA4), `notification` (Bible
  downloads) and `log` (every Kermit warning and error).
- A failure answers `{"ok": false, "error": "..."}`; the session keeps going.

In `state`, a data class is an object, a sealed subtype adds `"@type"`, an object is its name, a
`StringResource` is `{"@string": key, "text": ...}` resolved in the app language, and lambdas show as
`"<function>"`.

## What is real and what is not

- **Isolated storage.** Room and DataStore live under `user.home` (or `XDG_DATA_HOME`/`APPDATA`), the
  app moves legacy files in from the temp and working directories, and the Supabase session, the GA4
  client id and the RevenueCat id live in `java.util.prefs`, which on macOS is one plist shared with
  the desktop app. The CLI points all of them at its data directory,
  `tools/agent-cli/build/agent-cli-session/data` by default (one per worktree, set
  `AGENT_CLI_DATA_DIR` for another), so it never reads, moves or signs out the developer's own
  desktop data. Started without the script, it refuses to run while `XDG_DATA_HOME` or `APPDATA`
  points elsewhere.
- **The backend is real.** Supabase, RevenueCat and the AI study functions are the ones the desktop
  app calls, as an anonymous user. Generating a study costs what it costs from the app.
- **Analytics stay local.** GA4 is replaced by the `analytics` log.
- **No sign-in.** Signing in opens a browser on the desktop, so the session stays anonymous.
- **No Bible text on a fresh start**, as on a fresh install: download a version first.

  ```bash
  scripts/agent-cli.sh open BibleVersionSelectorRoute
  ```

  ```bash
  scripts/agent-cli.sh event OnDownload '{"id": "WEB"}'
  ```

  ```bash
  scripts/agent-cli.sh wait 'BibleVersionViewModel.uiState.data.ENGLISH[3].downloadStatus = Downloaded' --timeout 120000
  ```

- **Only ViewModels.** A `UiAction` shows in the log but nothing performs it (scrolling, a share
  sheet, the clipboard), and state a composable keeps in `remember` does not exist here.
  Android- and iOS-only ViewModels are not in the JVM graph, and neither are the ViewModels a slot
  builds from something other than a route, such as the day completion banner in the reader.

## How a route finds its ViewModels

Which ViewModels a route shows is decided inside the feature's entry composable, which the CLI can't
read. The routes come from `navigationSavedStateConfiguration`, and `ScreenCatalog` matches each one
to the ViewModels of the JVM Koin graph:

1. the exceptions listed in `AppScreens.viewModelsByRoute` (the main screen and its tabs, the Day
   screen with its study card, and the ViewModels named after something else);
2. otherwise, every ViewModel whose constructor takes the route;
3. otherwise, the ViewModel named after the route: `DeleteAccountNavRoute` → `DeleteAccountViewModel`.

`AppScreensTest` fails when a route matches nothing. A new route either follows one of the
conventions or gets a line in `AppScreens`. The mapping lives here and not next to each entry on
purpose: the features carry no code for a tool that never ships. `AppScreens.routeParameters` covers
an entry that hands its ViewModel another route built from its own (the share image sheet). `AppScreens.appViewModels` lists the ViewModels that
`AppRoot` and `RootAppNavDisplay` keep alive above every screen; `state --app` shows them.
