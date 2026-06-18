---
name: feedback_kdoc_simple_name_links
description: "KDoc links must use simple names (with an import added), never fully-qualified names, and must resolve."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 8359fdea-1c84-4f56-8d88-516dcbe17dee
---

In KDoc `[...]` references, use the **simple name** and add the corresponding import so the link
resolves — never a fully-qualified path. Add the import even if the symbol is only referenced from
KDoc (ktlint's no-unused-imports counts KDoc references as usage).

- `[com.quare....NetworkConnectivityObserver]` → `[NetworkConnectivityObserver]` + import it.
- `[io.github.jan.supabase.auth.Auth.signOut]` → `[Auth.signOut]` (with `Auth` imported).
- For a member of an imported type, qualify with the type: `[BookEntity.favoriteUpdatedAt]` (not the
  bare `[favoriteUpdatedAt]`, which won't resolve).

**Why:** unqualified/fully-qualified KDoc links show up as unresolved-symbol warnings in the IDE; the
user wants clean, resolvable doc links. Related: [[feedback_prefer_method_references]].
