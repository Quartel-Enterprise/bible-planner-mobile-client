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
REVENUECAT_PRO_KEY=your_pro_key_here
```

3.  Save the file.

## 3. Sync Project
After updating `local.properties`, you must sync your Gradle project to regenerate the `BuildKonfig` class.

- **Android Studio / IntelliJ**: Click "Sync Project with Gradle Files" (Elephant icon).
- **Command Line**: Run `./gradlew clean build`.

## Troubleshooting
- **Warning in Build Output**: If you see `⚠️ REVENUECAT_API_KEY not found...` or  `⚠️ REVENUECAT_PRO_KEY not found...`, ensure the key name matches exactly and the file is saved.
- **Paywall Fails to Load**: Verify your key is correct and that you have configured **Offerings** in the RevenueCat dashboard.
