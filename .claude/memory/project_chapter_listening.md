---
name: project-chapter-listening
description: "Listen to chapter (device TTS) feature, Oct/2026 branch feature/listen-chapter: architecture, monetization and gotchas found on device"
metadata:
  node_type: memory
  type: project
  originSessionId: 276d4a92-abed-41f4-9860-3cc1c3eea647
  modified: 2026-10-04T21:08:01.134Z
---

PR #566 (merged 2026-10-04, release 2.12.0) implements the Claude Design "Read Chapter v3 (Ouvir capítulo)".

- **Product decisions (user, 2026-10-04):** Android + iOS only (desktop/web hide it); freemium — free users unlock **one chapter per day** with a rewarded video (nothing free without the ad), Pro unlimited; quota counted **on the device** (DataStore + trusted date), RC `listening_rewarded_daily_limit` (default 1) and kill switch `chapter_listening_enabled` (code default **false** after review — must be published `true` in Firebase before release); end of chapter offers "Mark as read" (no auto-mark); "today's reading" mode plays the plan day of today in sequence from the reader shortcut.
- **Architecture:** `:core:chapter_listening` holds an app-scoped `ChapterListeningController` (Main-confined, generation counter drops stale engine callbacks) over `SpeechEngine` + `ListeningMediaSession`; Android = TextToSpeech + Media3 `SimpleBasePlayer` with **one media item per verse** (so notification/headset next/prev skip verses natively) in a `MediaSessionService` started by connecting a MediaController; iOS = AVSpeechSynthesizer + AVAudioSession + MPNowPlaying in Kotlin/Native (`UIBackgroundModes audio`). `GetAdjacentListeningChapter` is a core interface implemented by `:feature:read`.
- **Reader follows the player** with the new `Navigator.navigateReplacing(current, route)` (swaps the reader in place under the player sheet); `ListeningFollowRequests` marks readers opened that way so they don't pull the player back. Player stops 2 s after the last reader detaches.
- **Gotchas:** report PREPARING as playing to Media3 or the FGS drops between chapters; player chapter buttons must check the gate *before* moving (else a locked next chapter stopped the current one); Android voices have no names (UI shows "Voice N"); `compose.desktop.currentOs` needed in jvmTest for getString.

**Why:** large feature with several non-obvious design choices.
**How to apply:** extend listening through the controller and the gate (`ChapterListeningGate`), keep platform code behind the two interfaces.
/cre
