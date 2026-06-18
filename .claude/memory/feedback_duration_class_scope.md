---
name: feedback_duration_class_scope
description: "kotlin.time.Duration constants go as a private val in class scope, never inside a companion object."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 8359fdea-1c84-4f56-8d88-516dcbe17dee
---

Do not declare `kotlin.time.Duration` values inside a `companion object`. Put them as a `private val`
in the class scope (camelCase), e.g. `private val initialBackoff = 2.seconds`,
`private val pollInterval = 3.seconds`.

`const val` constants that the language allows (String/Int/etc.) may stay in the companion object;
the rule is specifically about `Duration` (which can't be `const` anyway).

**Why:** the user's preferred convention for this codebase — keeps duration tunables as plain class
fields rather than companion constants.

**How to apply:** when introducing a backoff/timeout/poll interval as a `Duration`, declare it as a
class-level `private val`. Related: [[feedback_use_suspendruncatching]].
