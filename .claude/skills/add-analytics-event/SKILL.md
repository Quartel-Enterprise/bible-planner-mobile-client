---
name: add-analytics-event
description: "Instrument analytics for a new or changed user action: classify each UiEvent's analytics decision, add the event to the catalog (docs/analytics), and add the name/param constants. Use whenever adding or modifying a feature, screen, dialog, or any user-facing action/interaction."
---

# Add analytics event

This skill makes sure a new user action ships with analytics, following the conventions in
[docs/analytics/README.md](../../../docs/analytics/README.md). It does **not** duplicate those
conventions — read that README (naming, standard parameter dictionary, tiers, and the
"Explicitly not tracked" list) and follow it exactly.

The compiler already forces the decision: every `UiEvent` case must declare an `analytics`
member (it implements the `UiEvent` marker in `ui/utils/.../presentation/UiEvent.kt`), so a
missing decision is a build error. This skill guides *which* decision to make and keeps the
docs catalog in sync (the part the compiler can't check).

## Workflow

For each user action added or changed (each `UiEvent` case):

### 1. Decide whether it needs an event

- If it isn't a user action at all — continuous scroll signals, animation-completion callbacks, VM
  lifecycle hooks, incoming messages/snackbar bridges (see the "Explicitly not tracked" section of
  the analytics README) → classify as `EventAnalytics.NotTracked` and stop here.
- Otherwise it needs an event — continue, **even if the action also navigates** (opens a
  screen/dialog/sheet, goes back, switches a bottom tab) **or seems low-value** (a menu
  open/close, a retry button, an intermediate picker step). There is no "covered by navigation"
  or "too minor to matter" opt-out: every discrete user click gets its own event. A click and the
  `destination_view` it triggers are different funnel signals (click-through rate vs. screen-view
  rate). **Never classify a real user click as `NotTracked`**; if it isn't trackable yet, that's a
  gap to fix now, not a reason to leave it untracked. `NotTracked` is reserved for things that are
  not clicks at all.

### 2. Name the event and its parameters

Follow the naming conventions (snake_case `<object>_<action>`, past tense; GA4 limits). Reuse
existing parameters from the standard parameter dictionary where possible.

- Add the event name as a `const val` in
  `core/provider/analytics/.../domain/model/AnalyticsEventNames.kt`.
- Add any genuinely new parameter keys as `const val` in
  `core/provider/analytics/.../domain/model/AnalyticsParams.kt`.

### 3. Classify the `UiEvent` case

Pick one `EventAnalytics` variant (`core/provider/analytics/.../domain/model/EventAnalytics.kt`).
Every tracked event is a `Track`, which splits on *how* it's emitted:

- `Track.Automatic(name, params)` — use when the event name and **all** params are known from the
  event object itself (its own fields or constants). The base `TrackedViewModel` emits it
  automatically; do **not** add a manual `trackEvent(...)` call for it.
- `Track.Manual(names)` — use when a param only exists after a domain call (e.g. `is_read` is
  the result of a toggle) or depends on `UiState`. Keep the explicit `trackEvent(...)` call
  inside the `handleEvent` branch. `names` must list every event the branch may actually emit for
  that case — the enforcement test (step 5) checks each one is really wired to a `trackEvent`
  call, so an incomplete or wrong `names` set fails the build.

Example (`feature/more`): `OnItemClick` carries its `type`, so its params are static →
`Track.Automatic`. Example (`feature/read`): `ToggleReadStatus` needs the post-toggle `is_read` →
`Track.Manual(AnalyticsEventNames.CHAPTER_READ_TOGGLED)`.

### 4. Update the catalog

- Create `docs/analytics/events/<event_name>.md` following the template of the existing event
  files (Tier/Domain header, When it fires, Trigger source, Parameters, Notes).
- Add the event to the index table in `docs/analytics/README.md`, in its domain section.

### 5. Verify

Compile the affected module (`./gradlew :feature:<name>:compileCommonMainKotlinMetadata`) and run
`./gradlew :feature:<name>:ktlintFormat`. A missing `analytics` declaration fails the build.

Then run `./gradlew :core:provider:analytics:jvmTest` — `AnalyticsCatalogTest` fails if a
`Track.Manual` name isn't wired to an actual `trackEvent` call in the same module, or if any
`Track.Automatic`/`Track.Manual` name is missing its `docs/analytics/events/<name>.md` entry.
