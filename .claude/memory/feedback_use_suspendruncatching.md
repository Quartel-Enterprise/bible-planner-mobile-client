---
name: feedback_use_suspendruncatching
description: Use the suspendRunCatching utility instead of manual try/catch(CancellationException)+catch(Exception) blocks.
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 8359fdea-1c84-4f56-8d88-516dcbe17dee
---

Prefer `com.quare.bibleplanner.core.utils.suspendRunCatching { ... }` over hand-written
`try { } catch (e: CancellationException) { throw e } catch (e: Exception) { ... }` blocks. It is
an `inline fun <T> (block: () -> T): Result<T>` defined in `core/utils/CoroutineUtils.kt` that
rethrows `CancellationException` and wraps any other exception in `Result.failure`. Handle outcomes
with `.onSuccess { }` / `.onFailure { error -> ... }` (both inline, so suspend calls and non-local
returns work inside them).

**Why:** removes boilerplate and guarantees the correct structured-concurrency behavior (never
swallow `CancellationException`) consistently across the codebase.

**How to apply:** apply everywhere the manual pattern appears, including pre-existing code, when it
is a clean swallow/log/retry. Do NOT convert blocks that catch only `CancellationException` for
special handling (e.g. `BibleVersionDownloadWorker` shows a "paused" state on cancel) — there the
intent differs. The module using it needs `implementation(projects.core.utils)`.
