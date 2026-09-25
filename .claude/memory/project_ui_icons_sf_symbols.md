---
name: project-ui-icons-sf-symbols
description: "ui/icons module (PR #451) — AppIcon enum renders real SF Symbols on iOS inside Compose, Material elsewhere; only Plans/Books/Profile migrated so far"
metadata:
  node_type: memory
  type: project
  originSessionId: 13717582-60ab-44f7-a4fa-16b527d5889b
  modified: 2026-09-25T01:52:05.443Z
---

ui/icons (merged 2026-09-24, PR #451): `AppIcon` enum pairs a Material ImageVector (property ref `Icons.Default::X`, lazy) with a Calf `SFSymbol` name; `Icon(icon = AppIcon.X)` / `rememberAppIconPainter` / `CommonIconButton(icon=)`. iOS actual rasterizes `UIImage.systemImageNamed` (17pt config) to an ImageBitmap at exact px size, cached, scaled by point size (0.75 of box height) and clamped to the box; falls back to Material if the symbol is missing.

**Why:** user wants native iOS icons on iOS; Calf only gives SF Symbol name constants (UIKit-only) and a font-based `calf-cupertino-icons` (rejected: outdated glyphs, 1,322 icons).

**How to apply:** remaining ~95 `Icons.*` usages in ~25 features (incl. Rounded/Outlined variants, ui/component heart/favorite) still to migrate — add enum entries, swap `Icons.Default.X`→`AppIcon.X`, `ImageVector` params→`AppIcon`, drop materialIconsExtended from migrated features. Calf `SFSymbol` only has SF Symbols 6+ names (e.g. `text.document`, not `doc.text`). Release notes deferred until migration is complete. Related: [[project-calf-native-tab-bar]].
