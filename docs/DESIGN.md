---
version: alpha
name: Bible Planner
description: >-
  Bible Planner is a Kotlin Multiplatform reading-plan tracker. The design
  system is rooted in Material Design 3 with a single Compose theme used across
  Android, iOS, and Desktop. Color, typography, spacing, and shape are derived
  from the canonical MD3 type scale and a project-specific color scheme
  generated from the brand seed `#4C5C92` (a calm periwinkle blue).

colors:
  # ── Primary ──
  primary: "#4C5C92"
  on-primary: "#FFFFFF"
  primary-container: "#DCE1FF"
  on-primary-container: "#344479"
  # ── Secondary ──
  secondary: "#595E72"
  on-secondary: "#FFFFFF"
  secondary-container: "#DEE1F9"
  on-secondary-container: "#414659"
  # ── Tertiary ──
  tertiary: "#755470"
  on-tertiary: "#FFFFFF"
  tertiary-container: "#FFD7F6"
  on-tertiary-container: "#5B3D57"
  # ── Error ──
  error: "#BA1A1A"
  on-error: "#FFFFFF"
  error-container: "#FFDAD6"
  on-error-container: "#93000A"
  # ── Background & Surface ──
  background: "#FAF8FF"
  on-background: "#1A1B21"
  surface: "#FAF8FF"
  on-surface: "#1A1B21"
  surface-variant: "#E2E1EC"
  on-surface-variant: "#45464F"
  surface-dim: "#DAD9E0"
  surface-bright: "#FAF8FF"
  surface-container-lowest: "#FFFFFF"
  surface-container-low: "#F4F3FA"
  surface-container: "#EEEDF4"
  surface-container-high: "#E9E7EF"
  surface-container-highest: "#E3E1E9"
  # ── Outline & Inverse ──
  outline: "#767680"
  outline-variant: "#C6C6D0"
  scrim: "#000000"
  inverse-surface: "#2F3036"
  inverse-on-surface: "#F1F0F7"
  inverse-primary: "#B6C4FF"
  # ── Brand accents (not part of the MD3 ColorScheme) ──
  gold: "#DAA520"
  success: "#2E7D32"

typography:
  display-large:
    fontFamily: Roboto
    fontSize: 57px
    fontWeight: 400
    lineHeight: 64px
    letterSpacing: -0.25px
  display-medium:
    fontFamily: Roboto
    fontSize: 45px
    fontWeight: 400
    lineHeight: 52px
    letterSpacing: 0px
  display-small:
    fontFamily: Roboto
    fontSize: 36px
    fontWeight: 400
    lineHeight: 44px
    letterSpacing: 0px
  headline-large:
    fontFamily: Roboto
    fontSize: 32px
    fontWeight: 400
    lineHeight: 40px
    letterSpacing: 0px
  headline-medium:
    fontFamily: Roboto
    fontSize: 28px
    fontWeight: 400
    lineHeight: 36px
    letterSpacing: 0px
  headline-small:
    fontFamily: Roboto
    fontSize: 24px
    fontWeight: 400
    lineHeight: 32px
    letterSpacing: 0px
  title-large:
    fontFamily: Roboto
    fontSize: 22px
    fontWeight: 400
    lineHeight: 28px
    letterSpacing: 0px
  title-medium:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: 500
    lineHeight: 24px
    letterSpacing: 0.15px
  title-small:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0.1px
  body-large:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
    letterSpacing: 0.5px
  body-medium:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0.25px
  body-small:
    fontFamily: Roboto
    fontSize: 12px
    fontWeight: 400
    lineHeight: 16px
    letterSpacing: 0.4px
  label-large:
    fontFamily: Roboto
    fontSize: 14px
    fontWeight: 500
    lineHeight: 20px
    letterSpacing: 0.1px
  label-medium:
    fontFamily: Roboto
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.5px
  label-small:
    fontFamily: Roboto
    fontSize: 11px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.5px

rounded:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  full: 999px

spacing:
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  2xl: 32px
  3xl: 48px

components:
  # ── Cards & surfaces ──
  card:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: 16px
  card-elevated:
    backgroundColor: "{colors.surface-container-high}"
    rounded: "{rounded.md}"
    padding: 16px

  # ── Buttons ──
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 16px
    height: 40px
  button-primary-pressed:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
  button-tonal:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 16px
    height: 40px
  button-outlined:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 16px
    height: 40px
  button-text:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: 12px

  # ── Segmented button (Plans screen sort toggle) ──
  segmented-button:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: 48px
  segmented-button-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"

  # ── List items (week + day rows) ──
  list-item:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    padding: 16px
    height: 72px
  list-item-selected:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"

  # ── Form controls ──
  checkbox-checked:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.xs}"
    size: 18px
  checkbox-unchecked:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface-variant}"
    rounded: "{rounded.xs}"
    size: 18px

  # ── Chips ──
  chip-hoje:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-small}"
    rounded: "{rounded.full}"
    padding: 8px
    height: 22px
  chip-suggestion:
    backgroundColor: "{colors.surface-container-low}"
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-large}"
    rounded: "{rounded.sm}"
    height: 32px

  # ── Navigation ──
  navigation-bar:
    backgroundColor: "{colors.surface}"
    height: 80px
  navigation-bar-item:
    textColor: "{colors.on-surface-variant}"
    typography: "{typography.label-medium}"
  navigation-bar-item-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.full}"

  # ── Progress indicators ──
  linear-progress-track:
    backgroundColor: "{colors.outline-variant}"
    rounded: "{rounded.xs}"
    height: 4px
  linear-progress-fill:
    backgroundColor: "{colors.primary}"
    rounded: "{rounded.xs}"
    height: 4px
  linear-progress-fill-done:
    backgroundColor: "{colors.success}"

  # ── Dividers ──
  divider:
    backgroundColor: "{colors.outline-variant}"
    height: 1px

  # ── Plans-screen-specific custom blocks ──
  date-box:
    backgroundColor: "{colors.surface-container-high}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    width: 44px
    height: 40px
  date-box-today:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
  accent-bar-today:
    backgroundColor: "{colors.primary}"
    width: 4px
    height: 40px
    rounded: "{rounded.xs}"
  accent-bar-done:
    backgroundColor: "{colors.success}"
    width: 4px
    height: 40px
    rounded: "{rounded.xs}"
  accent-bar-upcoming:
    backgroundColor: "{colors.outline-variant}"
    width: 4px
    height: 40px
    rounded: "{rounded.xs}"

  # ── Milestone / achievement accent ──
  badge-gold:
    backgroundColor: "{colors.gold}"
    textColor: "#000000"
    typography: "{typography.label-small}"
    rounded: "{rounded.full}"
    padding: 8px
---

## Overview

Bible Planner is a **calm, focused companion for daily Scripture reading**. The
brand wants to feel like the inside cover of a beloved hardcover Bible —
restrained, slightly serious, never gamified. Color and motion are used
sparingly so the user's attention stays on the reading content.

**Atmosphere:** monastic quiet, periwinkle blue as the only assertive hue, warm
near-white surfaces in Light and cool charcoal in Dark. Round shapes
(corner-radius `md`+) dominate; sharp edges only appear when stacking flush
rows inside a card.

**Aesthetic principles**

- **Material 3, no decoration.** The system is the unmodified MD3 type scale +
  ColorScheme. No bespoke fonts, no expressive shape morph, no FAB extensions.
- **Surface tonality > borders.** Hierarchy is communicated by elevating one
  surface tone over another (`surface-container-high` over `surface-container`)
  rather than by outline strokes.
- **Token-bound everything.** No raw hex in component code; everything resolves
  through `MaterialTheme.colorScheme.*` or `MaterialTheme.typography.*`. The
  three contrast levels (Standard / Medium / High) and Material You dynamic
  color are first-class.
- **Gold is sacred.** The `gold` accent token is reserved for milestone /
  achievement states (plan completed, streak reached). It never decorates
  navigation or routine UI.

## Colors

The palette comes from `ui/theme/src/commonMain/.../color/Color.kt` (raw
primitives) and `ColorScheme.kt` (the six MD3 `ColorScheme` instances:
Light/Dark × Standard/Medium/High contrast). The tokens above hold the
**Light + Standard contrast** values, which is the canonical reference. Dark
and contrast variants are generated from the same brand seed and are
maintained in lockstep — see the source files.

**Roles in this product**

| Role | Where it shows up |
|:---|:---|
| `primary` | Selected items, primary CTAs, today's accent bar, plan progress fill, the "Hoje" pill, the trending-up icon on the progress card |
| `primary-container` / `on-primary-container` | Selected day row highlight, today's date box, container-style buttons |
| `secondary-container` / `on-secondary-container` | Segmented button selected state, navigation-bar selected indicator pill |
| `tertiary` family | Reserved — not currently used in shipping screens |
| `error` family | Destructive dialogs (delete plan, delete version) |
| `surface` | Screen background, navigation bar background |
| `surface-container` | Week cards on the Plans screen, the progress card |
| `surface-container-high` | Date boxes, elevated/nested surfaces |
| `surface-container-highest` | Reserved for dialogs and bottom sheets |
| `outline-variant` | Dividers between day rows, week progress-bar track |
| `on-surface` | Primary body text |
| `on-surface-variant` | Secondary text, supporting text, inactive nav icons, captions |

**Brand accents (outside the MD3 ColorScheme)**

- **`gold` `#DAA520`** (Dark variant `#FFD700`) — exposed as
  `MaterialTheme.gold` extension via `ThemeExt.kt`. Use for milestone badges,
  perfect-week markers, plan-completed states. Never use for ordinary
  selection.
- **`success` `#2E7D32`** (Dark variant `#4ADE80`) — exposed as
  `MaterialTheme.success`. Use for "day completed" indicators (accent bar of
  finished days, checkmark fill on done checkboxes when not on the today row).

**Contrast contract**

The app exposes a `ContrastType` enum (Standard / Medium / High) that maps to
the matching `lightColorScheme` / `darkColorScheme` variant. Designers must
verify every new screen at all three contrast levels — `Medium Contrast` and
`High Contrast` are not afterthoughts.

## Typography

`AppTheme.kt` does **not** pass a `typography` parameter to `MaterialTheme`,
so the app uses the canonical **MD3 default type scale** unchanged. The font
family is `FontFamily.Default` (= Roboto on Android, San Francisco on iOS,
system default on Desktop). The token table above lists the Android/Roboto
values. Designers must not introduce custom font weights, sizes, or letter
spacing that aren't already in `MaterialTheme.typography`.

**Role usage in this product**

| Token | Used for |
|:---|:---|
| `headline-medium` | Hero metrics (e.g. `0,83%` on the progress card). Regular weight only — there is no Bold headline; if extra emphasis is needed, override per-call site with `.copy(fontWeight = FontWeight.Bold)`. |
| `title-medium` | Card section titles (`Semana 1`), large date numbers inside the date box |
| `title-small` | Card sub-titles (`Progresso do plano`), day-row primary label (`Dia 1`) |
| `body-large` | Verse text in reading view, list-item primary text |
| `body-medium` | Bible references (`Gênesis 1–3`), supporting list-item text |
| `body-small` | Captions, motivational messages, day counters (`2 de 7 dias`) |
| `label-large` | Status bar time, segmented-button labels, stats |
| `label-medium` | Navigation-bar labels |
| `label-small` | Tiny chips like `Hoje`, month label inside the date box |

## Layout

**Spacing scale** is a 4-pt grid mapped to t-shirt sizes (`xs` through `3xl`).
The scale was generated from MD3 conventions and is mirrored as variables in
the design tooling so design and code never drift.

| Token | Value | Common use |
|:---|:---|:---|
| `xs` | 4px | Tight gaps inside small chips, accent-bar margin |
| `sm` | 8px | Icon-to-label gap, internal chip padding |
| `md` | 12px | Card content padding, divider insets |
| `lg` | 16px | **Default screen margin**, default card outer padding |
| `xl` | 24px | Section separation, large card padding |
| `2xl` | 32px | Hero spacing, top-level section gaps |
| `3xl` | 48px | Rare; reserved for empty states |

**Phone screen frame:** 390 dp wide × 924 dp tall (mockup canvas). Horizontal
gutters are `lg` = 16dp on each side, leaving 358dp of content. Right-aligned
elements (e.g. progress stats, the checkbox column) anchor to the inner
content edge at x = `screen-width - lg` so growth never spills under the
status bar or navigation rail.

**Vertical rhythm**

- Status bar: top 0..52
- Top control (segmented button, search, etc.): y ≈ `2xl`
- Hero card / progress card: y ≈ `2xl + xl`
- List sections: stacked with `lg` gaps between cards
- Navigation bar: pinned to the bottom, full-bleed, `surface` fill

## Elevation & Depth

Depth is **never expressed with drop-shadows**. It is expressed by the
**surface-tonality ladder**:

```
surface-dim ──┐        (Dark mode "deepest" surface)
surface ──────┼── 0 dp — base screen
surface-bright┘        (Dark mode "brightest" surface)

surface-container-lowest ── 1 — modal/dialog floor
surface-container-low   ── 2 — input fields, low-priority cards
surface-container       ── 3 — default card (progress card, week cards)
surface-container-high  ── 4 — nested cards, date boxes, today indicator
surface-container-highest ── 5 — bottom sheets, side sheets, top app bars
```

Pick the *next* tonal step up to indicate a nested surface. A date box sits
inside a week card, so the card uses `surface-container` and the date box uses
`surface-container-high`. Going up two steps reads as "too far" and should be
avoided.

Inverse pair (`inverse-surface` / `inverse-on-surface`) is reserved for
snackbars and "hold to confirm" press feedback on selected rows.

## Shapes

The `rounded` scale defines exactly six corner radii.

| Token | Value | Used for |
|:---|:---|:---|
| `xs` | 4px | Accent bars, progress bar tracks/fills, checkbox |
| `sm` | 8px | Small chips, suggestion chips |
| `md` | 12px | **All cards** (progress card, week cards, date box) |
| `lg` | 16px | Active navigation-bar item indicator |
| `xl` | 24px | Bottom sheets, large dialogs |
| `full` | 999px | Pills: segmented button, navigation indicator, "Hoje" chip, FAB |

When stacking flush rows inside a single card (the expanded Week 1 in the
Plans screen), the **outer card** is the only piece that rounds: header has
`md`/`md`/0/0 corners (top only), middle rows are square, last row has
0/0/`md`/`md` (bottom only). Inner rows must not have their own radius — that
creates the "notch" artifact between adjacent rows.

## Components

### Progress card (Plans screen hero)

A `card` (surfaceContainer, `md` radius, `lg` padding). Anatomy:

1. Header row: `trending_up` icon (`primary`) + `title-small` "Progresso do plano".
2. Two-column row: `headline-medium` percentage on the left (`primary` color),
   right-aligned `label-large` count of completed/total + `body-small`
   remaining count.
3. `linear-progress-track` + `linear-progress-fill` spanning the inner width.
4. `body-small` motivational message on `on-surface-variant`.

### Week card

A `card` containing a 2-line `list-item` header (`title-medium` week name +
`body-medium` "X de 7 dias"), an inline `linear-progress` on the subtitle
line, and a trailing `expand_less` / `expand_more` icon. When expanded, day
rows (also `list-item`s) flow directly beneath, **sharing the same card** —
see Shapes for corner rules.

### Day row

A `list-item-selected` when it is today, otherwise a regular `list-item`.
Layout L→R: `accent-bar-{today|done|upcoming}` (4dp wide), `date-box` (with
date-box-today variant if today), label `title-small` "Dia N", supporting
`body-medium` reference, optional `chip-hoje` for the today row, trailing
`checkbox-{checked|unchecked}`.

**Completed-day treatment.** When a day is done, the row's text and date
elements dim to **opacity 0.55**; the **accent bar and the checked checkbox
stay at full opacity** so the "done" signal stays strong while the content
recedes. **Do not** add a strikethrough decoration on the label or supporting
text — the accent-bar + checkbox already carry the semantic, and a strike
line adds visual noise that fights the calm reading-app mood. Opacity alone
also preserves legibility for users who want to glance back at what they
read.

### Segmented button

Used at the top of the Plans screen for "Cronológico / Por livros" sort. Two
segments minimum, the selected one fills with `secondary-container` and shows
the check icon. **Do not** mix Segmented Button with the new Material 3
Expressive `Connected Button Group` in the same app unless the whole theme
migrates to Expressive.

### Navigation bar

Three items for this app: `Planos` (calendar icon), `Livros` (menu_book icon),
`Mais` (more_horiz icon). Selected indicator is a `full`-radius
`secondary-container` pill behind the active icon. Labels always shown, never
hidden.

### Chip "Hoje"

A `full`-radius, `primary`-filled pill 41×19 dp with `label-small` text. It
sits **inline** at the right of the day-row label, vertically centered on the
label baseline. It is the only place in the app where a tiny custom pill is
allowed; never replicate this size for other badge purposes — use
`chip-suggestion` or `chip-filter` instead.

## Do's and Don'ts

**Do**

- Bind every color and text style to a token. `Color(0xFF...)` and
  `TextStyle(fontSize = ...)` in feature modules are bugs.
- Verify every new screen in **all three contrast levels** and both Light and
  Dark before merging.
- Use the surface-tonality ladder (see Elevation & Depth) to express depth.
  One step up = nested, two steps up = wrong.
- Reuse the existing component recipes (cards, list items, segmented button).
  Building a one-off rounded surface for a new screen drifts the system.
- Treat `gold` and `success` as semantic — `gold = achievement`,
  `success = day completed`. They are not "warning yellow" or "OK green".

**Don't**

- Don't use raw color literals, bespoke fonts, or off-scale spacing /
  corner-radius values. If the scale is missing a value, the scale should grow
  — open a discussion before adding the value locally.
- Don't apply drop-shadows for hierarchy. Use surface tonality.
- Don't add per-screen text styles. The MD3 type scale is the entire set; if
  a role isn't there, you're trying to do something the system intentionally
  discourages.
- Don't use Bold headlines. The MD3 type scale defines headline weights as
  Regular. If a hero number must be emphasized, override per-call site rather
  than redefining the token.
- Don't add a divider above or below a selected row. The selection state
  layer already does the separating; an additional divider next to it reads
  as visual noise.
- Don't strikethrough completed-day text. Use opacity 0.55 on the row content
  and let the accent bar + checked checkbox carry the "done" semantic.
- Don't shrink chip-suggestion to badge-sized pills. Use `chip-hoje` (the
  only sanctioned tiny pill) or accept the M3 32dp chip height.

---

## Sources of truth

| Concern | File / location |
|:---|:---|
| Color primitives | `ui/theme/src/commonMain/kotlin/com/quare/bibleplanner/ui/theme/color/Color.kt` |
| Color schemes (6 variants) | `ui/theme/.../color/ColorScheme.kt` |
| Theme assembly + dynamic color | `ui/theme/.../AppTheme.kt` |
| `gold` & `success` extensions | `ui/theme/.../ThemeExt.kt` |
| Contrast enum | `ui/theme/.../model/ContrastType.kt` |
| Spacing & radius tokens | Figma variable collections `Spacing` and `Border Radius` |
| Color tokens (Sys/Ref) | Figma variable collections `Color · Ref` and `Color · Sys` |
| Text styles | Figma local text styles (`Display/*`, `Headline/*`, `Title/*`, `Body/*`, `Label/*`) |
| Plans-screen reference layout | Figma file `Bible Planner — Design System`, page `Proposta — Tela de Planos` |
