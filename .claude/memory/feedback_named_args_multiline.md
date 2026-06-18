---
name: feedback_named_args_multiline
description: "Calls/constructors with more than one argument should use named arguments, one per line."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 8359fdea-1c84-4f56-8d88-516dcbe17dee
---

When a function or constructor call passes more than one argument, write each argument as a named
argument on its own line:

```kotlin
val callback = ConnectivityCallback(
    connectivityManager = connectivityManager,
    onConnectivityChange = ::trySend,
)
```

Single-argument calls stay inline (e.g. `list.map(::mapModel)`, `?.let(::handle)`).

**Why:** the user's preferred call style for this codebase — readability and clear argument intent.
Related: [[feedback_prefer_method_references]].
