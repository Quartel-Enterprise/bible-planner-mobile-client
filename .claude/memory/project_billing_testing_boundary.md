---
name: project_billing_testing_boundary
description: "Billing use cases are decoupled from `expect class Purchases` via domain interfaces; orchestrators live in commonMain (unit-tested), RC adapters in mobileMain (untestable boundary)."
metadata: 
  node_type: memory
  type: project
  originSessionId: a9ea4a04-1da2-4425-bf79-ec48a79ff2fc
---

RevenueCat's `Purchases` is an `expect class` and the project has no mocking lib (hand-written fakes only), so anything depending directly on `Purchases` is not unit-testable. To test billing logic, the RC access is hidden behind domain-typed interfaces and `Purchases` is injected via `single<Purchases> { Purchases.sharedInstance }`.

Pattern in `core/provider/billing`:
- **commonMain interfaces (fakeable):** `IsProUserInRevenueCat`, `ObserveProVerificationRequired`, `IsProVerificationRequired`, `ObserveStoreSubscriptionStatus`, `BillingUserAccount`.
- **commonMain orchestrators (unit-tested in commonTest → jvmTest):** `IsProUserUseCaseImpl` (`inRevenueCat || !verificationRequired`), `GetSubscriptionStatusFlowUseCaseImpl` (flatMapLatest: verification off → Enterprise Pro, else store flow), `SyncBillingUserIdUseCase` (auth → logIn/logOut dispatch). Bound only in the mobile DI module; JVM keeps simple stub lambdas.
- **mobileMain RC adapters (the untestable boundary — only delegate to `Purchases`):** `IsProUserInRevenueCatUseCase`, `ObserveStoreSubscriptionStatusUseCase`, `BillingUserAccountImpl`, plus the thin `GetOfferings/GetPurchase/GetRestore/InitializeBilling` mobile use cases.

Billing tests run via `:core:provider:billing:jvmTest` (commonTest). The module has only common/jvm/ios test source sets — no android host test task. See [[project_revenuecat_identity]].
