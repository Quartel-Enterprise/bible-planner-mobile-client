---
name: project_revenuecat_identity
description: RevenueCat App User ID is tied to the Supabase user.id via SyncBillingUserId; enables cross-platform purchases and free-Pro comping via promotional entitlements.
metadata: 
  node_type: memory
  type: project
  originSessionId: a9ea4a04-1da2-4425-bf79-ec48a79ff2fc
---

RevenueCat identity is synced to the Supabase auth user: `SyncBillingUserId` (interface in `core/provider/billing` commonMain; commonMain impl `SyncBillingUserIdUseCase`) observes `ObserveAuthenticatedUserId` and dispatches to `BillingUserAccount` (mobile impl `BillingUserAccountImpl` calls `Purchases.awaitLogInResult(userId)` / `awaitLogOutResult()`). Launched from `InitializeAppContentUseCase`. JVM/desktop binds a no-op. `configureRevenueCat()` still configures anonymously at app start; the logIn happens after auth. See [[project_billing_testing_boundary]] for the decoupling.

Why it matters: before this, RevenueCat used anonymous per-install IDs, so purchases didn't sync across devices/platforms and there was no stable customer to comp.

**Granting Pro for free** (the chosen approach, zero app code): grant a **promotional/complimentary entitlement** in the RevenueCat dashboard/API to the customer whose App User ID = the user's Supabase `user.id`. `IsProUserInRevenueCatUseCase` then returns true automatically, so `IsProUserMobileUseCase` (`isProUserInRevenueCat() || !isProVerificationRequired()`) reports Pro. No `isForcedPro` table/column was added — do NOT add columns to `auth.users`. See [[project_remoteconfig_observe_first]] for the verification flag.
