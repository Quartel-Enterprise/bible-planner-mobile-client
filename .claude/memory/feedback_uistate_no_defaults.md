---
name: feedback-uistate-no-defaults
description: Data classes in this project (UiState and beyond) must not have default values on properties — the user dislikes defaults in any data class.
metadata: 
  node_type: memory
  type: feedback
  originSessionId: dd8eaf8a-7970-4b03-b0d9-4c3c7ae8cc2c
---

Data classes must not declare default values on their properties. Originally stated for UiState classes; in June 2026 the user generalized it: "particularmente nao gosto de parametros default em data classes" (said about a serialization DTO).

**Why:** Defaults hide what the actual value is at construction and let new fields silently default when added later, masking missing decisions. For UiState, every field must be passed explicitly when the ViewModel constructs the initial `MutableStateFlow`.

**How to apply:** Never write `val x: Type = default` in a data class. For UiState, pass values explicitly from the ViewModel (documented in `docs/architecture/state-management.md`). For serialization DTOs where JSON fields are optional, prefer avoiding the DTO altogether — e.g. read keys directly from the `JsonObject` (see `SessionUserMapper`) — rather than adding defaulted properties.
