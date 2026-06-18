---
name: feedback_parameterless_setup_beforetest
description: "a parameterless prepareScenario should be a @BeforeTest setup method, not called from each test's Given"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 4619b5d3-b8b7-4b88-8418-5f37eb6dd165
---

If the `prepareScenario(...)` factory ends up with no parameters (nothing varies per test), don't keep it as a function the tests call — convert it into a `@BeforeTest fun setUp()` that runs automatically. Drop the `prepareScenario()` call and the `// Given` comment from each test body; tests then read as just `// When` / `// Then`.

**Why:** `prepareScenario` exists to parameterize per-test setup. With no parameters there's nothing to parameterize, so a `@BeforeTest` expresses the shared, identical setup more directly and removes repetition at every call site.

**How to apply:** seen on a Room migration test — the parameterless `prepareScenario()` (after a constant-only param was removed, see [[feedback_no_constant_value_params]]) became `@BeforeTest fun setUp()`. Keep `@AfterTest` teardown alongside it. The `prepareScenario` convention still applies when setup DOES vary per test.
