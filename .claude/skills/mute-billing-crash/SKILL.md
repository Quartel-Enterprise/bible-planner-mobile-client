---
name: mute-billing-crash
description: "Find, verify and mute the known Google Play Billing ProxyBillingActivity crash in Firebase Crashlytics. Use when this billing crash reappears in Crashlytics (it gets a new issue id whenever the billing library version changes, so old mutes don't cover it)."
---

# Mute Play Billing ProxyBillingActivity crash

The Google Play Billing library has a long-standing bug: `ProxyBillingActivity.onCreate` throws
`NullPointerException` because the `PendingIntent` it reads is `null`. RevenueCat uses Play Billing
under the hood, so it surfaces in this app. The crash is **owned by the system** (the activity is
launched by the framework, not by our code), so there is nothing to fix on our side.

In practice it only affects automated test devices — Google Play Console Pre-Launch Report / Firebase
Test Lab robo-tests — which cannot complete a real purchase. Telltale signs: `X86_64` architecture on
a phone model that ships as ARM, and 100% of events in the first second of the session.

## Why it keeps reappearing

Crashlytics fingerprints the issue using the billing library version in the blamed frame
(e.g. `com.android.billingclient:billing@@8.3.0`). Every time the billing version changes — which
happens whenever the RevenueCat `purchases-kmp` dependency is bumped — the fingerprint changes and a
brand new issue id is created. A mute applied to the previous issue id does **not** cover the new one.
That is why it shows up again after you thought you had silenced it.

This skill is the deliberate, re-runnable answer to that: it re-verifies the signature and mutes the
current issue, instead of suppressing the crash silently and permanently in production code.

## Inputs

- **Android Firebase App Id** (required for every Crashlytics MCP call). Do not hardcode it here —
  `androidApp/google-services.json` is gitignored on purpose, so this committed file must stay free of
  Firebase identifiers. Read it at runtime from that local file: parse `client[].client_info.mobilesdk_app_id`
  (the `android` client). If the file is missing, ask the user for the App Id.

## Steps

1. **Find candidates.** Call `crashlytics_get_report` with `report: "topIssues"`,
   `filter.issueErrorTypes: ["FATAL"]`, and a generous page size (e.g. 25). Look for any issue whose
   title contains `ProxyBillingActivity` (it looks like
   `com.android.billingclient:billing@@<version> - com.android.billingclient.api.ProxyBillingActivity.onCreate`).
   If the report doesn't surface it, you can also ask the user for the issue id / console URL.

2. **Verify each candidate before muting.** For every matching issue:
   - `crashlytics_get_issue` to read `state`, `title`, `subtitle`, `signals`, `firstSeenVersion`,
     `lastSeenVersion`.
   - `crashlytics_batch_get_events` on the `sampleEvent` and confirm **all** of:
     - `blameFrame.symbol` == `com.android.billingclient.api.ProxyBillingActivity.onCreate`
     - `blameFrame.owner` == `SYSTEM`
     - `subtitle` mentions `NullPointerException` on `PendingIntent.getIntentSender()`
   - Note the test-device indicators (`device.architecture: X86_64`, `signals` containing
     `SIGNAL_EARLY`). These strengthen the "not a real user" conclusion but are not strictly required.
   - **If a candidate does NOT match this signature, do not mute it.** Report it to the user instead —
     a real, actionable billing crash must stay open.

3. **Mute the confirmed issues.** For each verified issue whose `state` is not already `MUTED`, call
   `crashlytics_update_issue` with `state: "MUTED"`.

4. **Report.** Tell the user, per issue: the issue id, title (with billing version), the
   verified signature, the test-device evidence, and the resulting state. If nothing matched, say so
   explicitly — do not mute anything speculatively.

## Guardrails

- Never mute an issue that fails the signature check in step 2. The whole point of this skill over a
  code-level handler is that a human-in-the-loop re-evaluation prevents over-suppression.
- Muting is reversible (`crashlytics_update_issue` with `state: "OPEN"`), so if in doubt, mute and tell
  the user how to reopen.
- The Crashlytics tools are deferred — load them first via ToolSearch:
  `select:mcp__plugin_firebase_firebase__crashlytics_get_report,mcp__plugin_firebase_firebase__crashlytics_get_issue,mcp__plugin_firebase_firebase__crashlytics_batch_get_events,mcp__plugin_firebase_firebase__crashlytics_update_issue`
