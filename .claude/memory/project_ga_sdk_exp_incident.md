---
name: project-ga-sdk-exp-incident
description: Crashlytics iOS issue 181010225a8f (NSDictionary nil key) is the 2026-09-29 Google Analytics sdk-exp server incident, not app code
metadata:
  type: project
---

Crashlytics iOS issue `181010225a8f7695edf124ff32b9c406` (`-[__NSDictionaryM setObject:forKeyedSubscript:]: key cannot be nil`, crashed thread only `_dispatch_call_block_and_release`) is firebase/firebase-ios-sdk#16728: a malformed `sdk-exp` experiment payload served by Google Analytics from 2026-09-29 00:41 UTC, rolled back, fully gone 06:52 UTC. It hit every SDK version (we ship 12.13.0) on 2.8.5 and 2.9.0; our 6 events were 01:29–02:36 UTC. No app change or SDK bump needed.

**Why:** the stack has no app or Kotlin frames (GULMutableDictionary writes via dispatch_async and drops the APMETaskManager caller), so it looks like our bug but isn't. The kotlin TerminateHandler frames are just the terminate-handler chain [[project-ios-kotlin-exception-hook]].

**How to apply:** if this signature reappears, check its event times against a new Analytics incident before touching code; the mitigation is server-side (or `setAnalyticsCollectionEnabled(false)` via remote switch).
