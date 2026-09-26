---
name: project-tunescout-ktlint-port
description: Sibling repo TuneScout (~/StudioProjects/musicAi/TuneScout) is where new custom ktlint rules originate; Sep/2026 port brought 8 rules, skipped kdoc-* and top-level-function-ownership on purpose.
metadata:
  type: project
---

TuneScout (`/Users/pierrevieira/StudioProjects/musicAi/TuneScout/tools/ktlint_custom_rules`) is the user's sibling project and the usual source of new custom ktlint rules; same ktlint version (1.8.0), so rules port with a package/rule-set-id rename (`tunescout-style` → `bible-planner-style`).

On 2026-09-25 ported: composable-naming-suffix, companion-object-constants, top-level-val-ownership, interface-implementation-separate-files, redundant-private-constructor-property, constructor-property-order, unused-function-parameter, dto-serial-name.

Deliberately NOT ported:
- `kdoc-only-comments` / `kdoc-tags` — contradict this repo's no-comments-in-production rule ([[feedback-no-comments]]).
- `top-level-function-ownership` — ~67 hits were mostly KMP expect/actual factories, Koin setup and iOS `MainViewController`, which must stay top-level.

Adaptations: the composable suffix list is bible-planner's own (adds Section, Item, Chip, Overlay, Pane, Display, Root, Skeleton-not-Shimmer, etc.; drops music-only Artwork/Cover); `dto-serial-name` only applies to `@Serializable` `*Dto` classes (ProfileDto is hand-built from JsonObject). The root composable `App` became `AppRoot`.

**How to apply:** when the user asks to sync rules from TuneScout again, diff the rule directories, measure hits by running TuneScout's jar with the ktlint CLI over this repo before porting, and re-check the rejected ones only if the no-comments or KMP constraints change.
