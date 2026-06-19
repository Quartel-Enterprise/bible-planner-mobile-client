---
name: project_remoteconfig_observe_first
description: RemoteConfig is observe-first (Flow real-time); Get use cases derive from observe().first(); platform layer is RemoteConfigDataSource.
metadata: 
  node_type: memory
  type: project
  originSessionId: a9ea4a04-1da2-4425-bf79-ec48a79ff2fc
---

In `core/remote_config`, `RemoteConfigService` (commonMain interface) is **observe-first**: it exposes `observeBoolean/observeInt/observeString(key, defaultValue): Flow<T>`, backed by Firebase real-time updates (`addConfigUpdateListener` + `activate()`). `RemoteConfigServiceImpl` builds these Flows with `callbackFlow` (initial value + re-read on each config update) and `distinctUntilChanged`.

**Per-call configurable defaults:** the platform `RemoteConfigDataSource` returns **nullable** (`getBoolean/Int/String(key): T?` → null when the key is absent, i.e. Firebase value `source == STATIC`), and `RemoteConfigServiceImpl` coalesces with the caller's default (`getBoolean(key) ?: defaultValue`). The public use cases `Observe*RemoteConfig`/`Get*RemoteConfig` expose `invoke(key, default = false/0/"")`. NOTE: these are regular `interface`s (not `fun interface`) because Kotlin forbids default parameter values on a functional interface's SAM — so fakes must be objects, not lambdas.

The platform-injected piece is the low-level interface **`RemoteConfigDataSource`** (`suspend getBoolean/Int/String(key): T?` + `addConfigUpdateListener(onUpdate): Cancellable`), implemented by `AndroidRemoteConfigService` (checks `getValue(key).source != VALUE_SOURCE_STATIC`), `DesktopRemoteConfigService` (returns null → always caller default), and the Swift `IosRemoteConfigService` (`value.source != .static`). The split exists because **the iOS impl is Swift and Swift cannot build Kotlin Flows** — Flow construction lives in commonMain; Swift only implements the suspend getters + the callback listener (returning a `Cancellable`).

`GetBooleanRemoteConfig`/`GetIntRemoteConfig`/`GetStringRemoteConfig` are one-shot reads (`observeX(key, default).first()`). New code that wants live flags injects `Observe*RemoteConfig`; pass a non-zero `default` where the type-zero is unsafe (e.g. `MAX_FREE_NOTES`).

Android fetch latency was set to `minimumFetchIntervalInSeconds = 0` to match iOS (fresh fetch per cold start). See [[project_tester_user_property_flow]] for the `is_tester` consumer.
