---
name: shared-element-stuck-placement
description: "Compose 1.12.1 bug (CMP-10888) — a sharedElement in a TopAppBar title whose transition ends within ~2 frames stays at the source position; use Modifier.sharedElementWithRelayout for every top-bar title target."
metadata:
  node_type: memory
  type: project
  originSessionId: 1ffc0e20-9f0d-44d3-81fe-0d899388e4d8
  modified: 2026-10-04T06:59:34.095Z
---

Compose animation/ui 1.12.1 bug (found 2026-10-04, paywall "Become Pro" title missing on first open): when a shared transition ends after only ~2 approach frames, the target `sharedElement` content keeps its last approach offset (the source's position). After `isTransitionActive` flips false, the approach pass re-measures but never re-places it, and a same-frame relayout request is dropped. Inside a clipping parent (TopAppBar), it disappears entirely, with no accessibility node either.

Triggers: a slow first composition of the target screen (cold start, busy device; ~60–75% under 6 busy-loop shells on the emulator), and **deterministically with animator_duration_scale 0** (the "Remove animations" setting). In tests: `runComposeUiTest(effectContext = MotionDurationScale 0f)` reproduces it on JVM (see PaywallTopBarUiTest).

Reported as CMP-10888 (2026-10-04, PR #557); minimal repro needs the target inside an M3 `TopAppBar` (a plain Box doesn't reproduce).

Fix: `Modifier.sharedElementWithRelayout(sharedTransitionScope, animatedVisibilityScope, key)` (ui/utils/transition) wraps `sharedElement` with the now-internal `relayoutAfterSharedTransition` node (invalidatePlacement one frame after the transition deactivates). Use it for any shared-element target in a TopAppBar **title** slot.

Audit 2026-10-04 (branch fix/shared-element-stuck-position) on an emulator with animations off: only TopAppBar title targets break. Release notes title 5/5 missing, book details title 1/10 under CPU load (it appears after a load, so it usually misses the transition), Day header 0/10 but the same structure. Moved to the helper: become_pro (paywall+profile), release notes, book name in book details (SharedTransitionModifierFactory.getTopBarBookNameSharedTransitionModifier, while the list source stays plain), Day header. Unaffected: list/body elements (books, book_details cards, reading_plan), FavoriteIcon in TopAppBar **actions**, nav bar/rail keepStillAcrossTabs (source == target position). Animations on: 0/30 even under load. Per-screen *TopBarUiTest regression tests use `AnimationsDisabled` + `SharedTransitionTestContent` from :ui:testing (documented in docs/testing/compose-ui-tests.md), and they fail without the relayout. No ktlint enforcement yet (deferred). Remove it all once a Compose release fixes CMP-10888 (open, no comments as of 2026-10-04).

Related: [[stable-window-insets-workaround]]
