---
name: project-calf-adaptive-components
description: Calf adaptive components adopted for native iOS UI (switch, slider, spinner, date/time pickers, simple alerts, dropdown menus) — wrappers, gotchas and what stays Material.
metadata:
  type: project
---

Branch `enhancement/native-ios-components` (2026-09-24): user wants native iOS *components* (e.g. iOS toggle), not just icons.

- Direct Calf in features: `AdaptiveSwitch`, `AdaptiveSlider`, `AdaptiveCircularProgressIndicator` (indeterminate only; PlanProgress determinate stays Material), `AdaptiveDatePicker`/`AdaptiveTimePicker` + `rememberAdaptive*State` inside the existing Material dialogs.
- Wrappers in ui/component: `AppAlertDialog` (UIAlertController; only title+text+≤2 buttons dialogs — custom-content/loading/textfield dialogs stay Material) and `BoxScope.AppDropdownMenu(items: List<AppDropdownMenuItem>)` (UIMenu; must sit in a Box with the anchor; selected → checkmark SF Symbol, icon via public `AppIcon.sfSymbol`).
- Gotchas: Calf's iOS alert calls `onDismiss` again when it leaves composition after a confirm tap → `AppAlertDialog` resolves only the first callback. Calf Material alert uses filled Buttons → wrapper passes TextButtons. iOS 26 LiquidGlass switch/slider ignore `colors` and `steps` (reader PlainSlider snaps manually, iOS-only via `ui/utils isIos`; Android keeps the custom slider). ColorPickerSlider (gradient) stays Material.
- Presented UIKit (alerts, menus) inherit traits from the window, not the VC → MainViewController sets `overrideUserInterfaceStyle` on the window too.

Related: [[project-calf-native-tab-bar]], [[project-ui-icons-sf-symbols]].
