---
name: project_loadable_per_field_loading
description: Loadable<T> in core/model + merge/scan reducer pattern for per-field independent section loading (no full-screen Loading state).
metadata: 
  node_type: memory
  type: project
  originSessionId: def50517-d9d9-4d50-9155-deace797e9c9
---

`Loadable<T>` lives in **`core/model`** (`com.quare.bibleplanner.core.model.loadable.Loadable`, public) — a reusable sealed type: `Loading` | `Loaded(value)`, with `fun <T> Loadable<T>.valueOrNull(): T?`. Intended to be reused across features for granular load states.

**The strategy** (first applied to the More screen, see [[project_shimmer_wrapper]]): instead of one `combine` that gates the whole screen behind the slowest flow, the UiState is a single `data class` where each flow-derived field is a `Loadable<T>`. The factory builds it with:
- `initialState()` = every field `Loadable.Loading` + synchronous fields (consts, current date).
- `create()` = `merge(...)` of each source flow `.map { value -> { state -> state.copy(field = Loadable.Loaded(value)) } }` (a reducer `(State) -> State`), then `.scan(initialState()) { state, reduce -> reduce(state) }`.

Each field flips Loading→Loaded independently → each UI section shows shimmer until its own data arrives; no global spinner. Visibility flags as `Loadable<Boolean>` use `valueOrNull() == true` (Loading = hidden). Shimmer rendering via the `ui/component` shimmer wrapper.

Tradeoff accepted by the user: fast fields (DataStore/Room) shimmer briefly on cold start instead of blocking the screen.
