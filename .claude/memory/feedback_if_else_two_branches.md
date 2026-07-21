---
name: feedback-if-else-two-branches
description: "Use if/else instead of `when` when there are only two branches (condition + else)."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 4a9bce83-e011-440a-8d99-55ae5fa03156
  modified: 2026-07-20T03:51:26.665Z
---

Prefer `if/else` over `when { cond -> ...; else -> ... }` whenever the expression has only two
branches. Reserve `when` for three or more, and for exhaustive sealed-type dispatch.

**Why:** `when` with a single condition plus `else` is just an `if` with heavier syntax; the user
called this out on `PlatformFile.toPickedEvent`, which used a two-clause `when {}`.

**How to apply:** count the branches before reaching for `when`. Two → `if/else`. Note ktlint may
then collapse the body onto the signature line (`= if (...) {`) — run `ktlintFormat` and keep its
output. Related: [[feedback_named_args_multiline]], [[feedback_prefer_method_references]].
