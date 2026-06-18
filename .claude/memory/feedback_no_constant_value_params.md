---
name: feedback_no_constant_value_params
description: "don't add a function/prepareScenario parameter whose callers all pass the same literal; hoist it to a companion-object const instead"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 4619b5d3-b8b7-4b88-8418-5f37eb6dd165
---

If every caller of a parameter passes the same literal value, don't make it a parameter — hoist it to a constant and use it directly inside the function. Delete the inlined string literal too.

**Why:** a parameter that never varies is noise; it pretends to be configurable but isn't. A constant states the value is fixed and keeps call sites clean.

**How to apply:** seen with a test's `prepareScenario(johnVersionStatus = "DONE")` where all tests passed `"DONE"` — removed the parameter and referenced a constant in the SQL insert. Place string constants in a `companion object` at the END of the (test) class, named `UPPER_SNAKE_CASE`, e.g. `private const val DONE_STATUS = "DONE"`. Applies to production code too, not just tests. Note: this companion-object rule is for plain string/const values — `kotlin.time.Duration` constants are the exception and stay as a camelCase private val in class scope (see [[feedback_duration_class_scope]]). Related: [[feedback_named_args_multiline]].
