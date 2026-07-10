# login_started

**Tier:** P1 | **Domain:** Auth

Captures the intent to sign in: the user tapped one of the social login buttons on the login bottom sheet. Together with [login](login.md) it yields the provider-level conversion rate of the sign-in flow and shows where users abandon (native sheet, network, provider errors).

## When it fires

The user taps the Google or Apple button on the login bottom sheet, before the native sign-in flow opens.

## Trigger source

`feature/login/.../LoginViewModel.kt` — `LoginUiEvent.SocialLoginClick`

## Parameters

| Name | Type | Example | Description |
|---|---|---|---|
| `method` | string | `apple` | Auth provider (`google` \| `apple`), from `LoginUiEvent.SocialLoginClick.provider` |

## Notes

- Every `login_started` should terminate in exactly one of [login](login.md), [login_failed](login_failed.md) or [login_cancelled](login_cancelled.md); sessions that end with none of them indicate an unhandled abandonment path.
- The login sheet impression itself is covered by `destination_view` (`destination_name=login`), so no separate "login sheet shown" event exists.
