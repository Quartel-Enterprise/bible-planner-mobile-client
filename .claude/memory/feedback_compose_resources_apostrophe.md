---
name: feedback_compose_resources_apostrophe
description: "In Compose Resources strings.xml, write apostrophes literally — never escape with backslash."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 6c729a20-b9f6-4994-ae9d-44d2e43f82ab
---

In this project's `composeResources/.../strings.xml` files, apostrophes must be written as a literal `'` (e.g. `doesn't`, `Couldn't`, `You're`). Do NOT use the Android-style backslash escape `\'`.

**Why:** Compose Multiplatform Resources does not process the `\'` XML escape — it renders the backslash literally, so the user sees `doesn\'t` on screen. The rest of the project follows the literal-apostrophe convention (e.g. `feature/logout`, `feature/read`, `feature/more`).

**How to apply:** When adding/editing any `strings.xml` under `composeResources`, type apostrophes directly. Quotes (`"`) inside text are fine as-is too (see `feature/read` examples). Applies to all locales (en/pt/es).
