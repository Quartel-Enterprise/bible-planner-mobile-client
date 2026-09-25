---
name: project-calf-adaptive-components
description: Calf adaptive components adopted for native iOS UI (switch, slider, spinner, date/time pickers, simple alerts, dropdown menus) — wrappers, gotchas and what stays Material.
metadata:
  type: project
---

Branch `enhancement/native-ios-components` (2026-09-24): user wants native iOS *components* (e.g. iOS toggle), not just icons.

- Switch goes through ui/component `AppSwitch` (expect/actual): iOS 26 → Calf `LiquidGlassSwitch`, older iOS → `CupertinoSwitch`, both with the app primary as checked track (user wants the theme color, not iOS green); Android/JVM → Material `Switch`.
- Direct Calf in features: `AdaptiveSlider`, `AdaptiveCircularProgressIndicator` (indeterminate only; PlanProgress determinate stays Material). Date/time pickers go through ui/component/date: bodies `DatePickerDialogContent` / `TimePickerDialogContent`, hosted by `PickerDialog` (standalone) or `PickerDialogSceneContent` (inside a Nav3 `DialogSceneStrategy.dialog(pickerDialogProperties)` entry), date→time swap via `PickerContentCrossfade`.
- iOS picker dialogs are a native modal (2026-09-25): `NativePickerModal` presents a transparent `ComposeUIViewController` (overFullScreen) with our own scrim; UIKit animates the whole view (enter fade + 1.08→1 scale, exit fade on dispose), so Calf's UIKitView calendar/wheel moves with the Compose canvas. Swap = UIKit snapshot crossfade. Why: UIKitView sits *below* the Metal canvas, so Compose Dialog animations leave holes/blank frames, and a Dialog nested in a Nav3 dialog makes native taps fall through as "outside" (pops the entry). iOS `pickerDialogProperties` makes the Nav3 dialog invisible (transparent scrim, no animation). Android/JVM keep Compose Dialog + AnimatedContent.
- Calf's `UIDatePicker()` has a zero frame, so the time wheel fills max constraints; `nativeTimePickerSize()` gives it the `sizeToFit` size, and Material `TimePickerDialog` can't host it (content ≥200dp tall and wider than tall → its "landscape" layout takes full height). UICalendarView draws "today + selected" as a solid tint circle — native, no API to change; user chose to keep it.
- Wrappers in ui/component: `AppAlertDialog` (UIAlertController; only title+text+≤2 buttons dialogs — custom-content/loading/textfield dialogs stay Material) and `BoxScope.AppDropdownMenu(items: List<AppDropdownMenuItem>)` (UIMenu; must sit in a Box with the anchor; selected → checkmark SF Symbol, icon via public `AppIcon.sfSymbol`).
- Gotchas: Calf's iOS alert calls `onDismiss` again when it leaves composition after a confirm tap → `AppAlertDialog` resolves only the first callback. Calf Material alert uses filled Buttons → wrapper passes TextButtons. Calf's `Adaptive*` iOS 26 LiquidGlass switch/slider ignore `colors` and `steps` (reader PlainSlider snaps manually, iOS-only via `ui/utils isIos`; Android keeps the custom slider). ColorPickerSlider (gradient) stays Material.
- Presented UIKit (alerts, menus) inherit traits from the window, not the VC → MainViewController sets `overrideUserInterfaceStyle` on the window too.
- iOS 27 (simulator, 2026-09-24): every UIMenu opens with the software keyboard and covers its source button — Apple's Files app does the same, so it's system behavior, not Calf/our code. Don't chase it. A plain UIView added over Compose gets no touches; native controls need `UIKitView`.

Related: [[project-calf-native-tab-bar]], [[project-ui-icons-sf-symbols]].
