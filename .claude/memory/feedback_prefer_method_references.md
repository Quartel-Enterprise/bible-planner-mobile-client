---
name: feedback_prefer_method_references
description: Prefer a method/function reference (::fn) over a lambda that only forwards its arguments.
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 8359fdea-1c84-4f56-8d88-516dcbe17dee
---

When a lambda does nothing but forward its argument(s) to a single function, use a function reference
instead: `{ isConnected -> trySend(isConnected) }` → `::trySend`; `ConnectivityCallback(manager) { trySend(it) }`
→ `ConnectivityCallback(manager, ::trySend)`.

**Caveat (important):** this only works when the target higher-order parameter accepts the same kind
of function. A **`suspend`** function reference is NOT assignable to a non-suspend lambda parameter,
so `list.map { mapModel(it) }`, `?.let { mapModel(it) }`, `forEach { applyRemote(it) }` can NOT become
`::mapModel`/`::applyRemote` when those are `suspend` — `map`/`let`/`forEach` take non-suspend lambdas
and only the inline lambda body may call a suspend function. Functions whose parameter is itself
`suspend` (e.g. `Flow.collect`) DO accept the reference: `.collect(::applyRemote)`.

**Why:** less boilerplate, clearer intent. Related: [[feedback_named_args_multiline]].
