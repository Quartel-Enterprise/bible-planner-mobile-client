# RevenueCat Setup Guide

This project uses [RevenueCat](https://www.revenuecat.com/) for In-App Purchases and Subscriptions. To enable these features, you must configure a valid API Key.

## 1. Obtain an API Key
1.  Log in to your [RevenueCat Dashboard](https://app.revenuecat.com/).
2.  Select your project (or create one).
3.  Navigate to **Project Settings** > **API Keys**.
4.  Copy the **Public API Key** (usually starts with `goog_` or `appl_`, but we use a consolidated key if you are using the same project for both platforms or separate keys mapped to this single configuration).

## 2. Configure local.properties
The `local.properties` file is used to store sensitive configuration and is not checked into version control.

1.  Open `local.properties` in the root of the project.
2.  Add the following line:

```properties
REVENUECAT_TEST_API_KEY=your_test_key_here
REVENUECAT_PLAY_STORE_API_KEY=your_play_store_key_here
REVENUECAT_APP_STORE_API_KEY=your_app_store_key_here
REVENUECAT_WEB_BILLING_API_KEY=your_web_billing_key_here
REVENUECAT_WEB_PURCHASE_LINK=https://pay.rev.cat/your_web_purchase_link_token
```

3.  Save the file.

> CI writes `local.properties` from the `LOCAL_PROPERTIES` secret. The two Web Billing values live in their
> own repository secrets instead — `REVENUECAT_WEB_BILLING_API_KEY` and `REVENUECAT_WEB_PURCHASE_LINK`.
> No job consumes them yet (`release.yml` only builds Android and iOS); the desktop packaging job must
> append them to `local.properties` after the `LOCAL_PROPERTIES` step:
>
> ```yaml
> printf 'REVENUECAT_WEB_BILLING_API_KEY=%s\n' "${{ secrets.REVENUECAT_WEB_BILLING_API_KEY }}" >> local.properties
> printf 'REVENUECAT_WEB_PURCHASE_LINK=%s\n' "${{ secrets.REVENUECAT_WEB_PURCHASE_LINK }}" >> local.properties
> ```

## 3. Sync Project
After updating `local.properties`, you must sync your Gradle project to regenerate the `BuildKonfig` class.

- **Android Studio / IntelliJ**: Click "Sync Project with Gradle Files" (Elephant icon).
- **Command Line**: Run `./gradlew clean build`.

## 4. Desktop (JVM) billing

`purchases-kmp` has no desktop target, so the JVM app talks to RevenueCat over its public REST API and
delegates the checkout to a Web Purchase Link opened in the system browser:

| Concern | How desktop solves it |
|---|---|
| Entitlement / subscription status | `GET /v1/subscribers/{app_user_id}` with the **Web Billing public key** (`rcb_…`) |
| Paywall packages and prices | `GET /v1/subscribers/{app_user_id}/offerings` + `GET /rcbilling/v1/subscribers/{app_user_id}/products` |
| Purchase | Web Purchase Link (`https://pay.rev.cat/<token>/<app_user_id>?package_id=<package>`) opened in the browser, then the app polls the subscriber endpoint until the entitlement turns active (10 min) |
| Restore | Re-fetch the subscriber for the same `app_user_id` — no separate restore flow |

Where the values come from:

- `REVENUECAT_WEB_BILLING_API_KEY`: **Project Settings > API Keys**, the public key of the *Web Billing*
  app (`rcb_…` for production, `rcb_sb_…` for sandbox).
- `REVENUECAT_WEB_PURCHASE_LINK`: **Funnels > Purchase Links**, "Share URL" of the link created for the
  `default` offering — copy only the part before the app user id (e.g. `https://pay.rev.cat/abc123`). The
  same dialog has a sandbox URL (`https://pay.rev.cat/sandbox/<token>`); pair it with the `rcb_sb_…` key
  to test checkout with Stripe test cards instead of real charges.

The app appends `?package_id=` with the package **lookup key** (`$rc_monthly` / `$rc_annual`) so the plan
picked on the desktop paywall opens straight at checkout — the internal `pkge…` id does *not* work there.

When either value is blank the desktop app treats the user as **Free** and purchases fail with
`BillingUnavailableException`, so a build shipped without the keys never hands out Pro for free.

Because the entitlement is store-agnostic, a subscription bought on Google Play or the App Store unlocks
Pro on desktop for the same account (the RevenueCat `app_user_id` is the Supabase `user.id`).

## Troubleshooting
- **Warning in Build Output**: If you see `⚠️ REVENUECAT_API_KEY not found...` or  `⚠️ REVENUECAT_PRO_KEY not found...`, ensure the key name matches exactly and the file is saved.
- **Paywall Fails to Load**: Verify your key is correct and that you have configured **Offerings** in the RevenueCat dashboard.
