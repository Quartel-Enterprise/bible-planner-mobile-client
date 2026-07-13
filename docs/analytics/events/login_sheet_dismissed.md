# login_sheet_dismissed

**Tier:** P2 | **Domain:** Auth

Captures the user closing the login bottom sheet without completing (or even starting) a sign-in attempt. Distinguishes an explicit close from a soft "not now" dismissal, so drop-off at the very top of the funnel can be split from provider-specific cancellations.

## When it fires

The user dismisses the login bottom sheet either by tapping the close button or by tapping "Not now" (when the sheet offers that option), without starting a social sign-in flow.

## Trigger source

`feature/login/.../LoginViewModel.kt` — two cases:

- `LoginUiEvent.DismissClick` — `method=close_button`
- `LoginUiEvent.NotNowClick` — `method=not_now`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `close_button` | How the sheet was dismissed (`close_button` \| `not_now`) |

## Notes

- This is distinct from [login_cancelled](login_cancelled.md), which only fires once a native provider sign-in flow was actually started and then closed by the user.
- The sheet impression itself is covered by `destination_view` (`destination_name=login`).
