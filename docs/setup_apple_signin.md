# Apple Sign-In Setup Guide

This project supports Sign in with Apple on all platforms through [Supabase Auth](https://supabase.com/docs/guides/auth/social-login/auth-apple):

- **iOS**: native Sign in with Apple sheet (`appleNativeLogin()` from supabase compose-auth). Requires the
  "Sign in with Apple" capability (`iosApp/iosApp/iosApp.entitlements`).
- **Android**: browser OAuth flow. Supabase redirects back into the app through the
  `bibleplanner://auth-callback` deep link (see `SupabaseDeeplinkHandler` and the intent filter in
  `androidApp/src/main/AndroidManifest.xml`).
- **Desktop (JVM)**: browser OAuth flow. supabase-kt starts a local callback server on an ephemeral port
  (`JvmAppleSignInStarter`).

## Apple Developer artifacts

All created under the **Quare Software** team (`TTV2A365LG`) at
[developer.apple.com](https://developer.apple.com/account/resources):

| Artifact | Value | Purpose |
|---|---|---|
| App ID | `com.quare.bibleplanner.BiblePlanner` | Has the "Sign in with Apple" capability (primary App ID). Used as client ID by the native iOS flow. |
| Services ID | `com.quare.bibleplanner.BiblePlanner.signin` | Client ID for the web OAuth flow (Android/desktop). Its Return URL is the Supabase callback. |
| Key | `Bible Planner Sign in with Apple Key` (Key ID `S6T3824BDQ`) | Private key (`AuthKey_S6T3824BDQ.p8`) used to sign the client secret. **Apple only lets you download the .p8 once** — keep a backup in a safe place. |

### Regenerating the provisioning profiles (fastlane match)

The iOS release build signs with a provisioning profile managed by [fastlane match](release-process.md)
and stored encrypted in the private `bible-planner-certs` repo. The release workflow consumes it with
`match(readonly: true)` (see `fastlane/Fastfile`), so **CI never recreates profiles** — that is a
one-time admin action done locally.

When the "Sign in with Apple" capability is enabled on the App ID, **existing provisioning profiles are
not updated automatically** — they must be regenerated, or the App Store build fails with:

> Provisioning profile "match AppStore com.quare.bibleplanner.BiblePlanner" doesn't include the Sign In
> with Apple capability.

After enabling (or changing) any capability on the App ID, regenerate and re-push the profiles:

```bash
# Verify "Sign In with Apple" is checked on the App ID com.quare.bibleplanner.BiblePlanner
# at developer.apple.com → Identifiers, then force-recreate the appstore profiles:
MATCH_PASSWORD='<certs repo passphrase>' fastlane match appstore --force
```

`--force` recreates the certificate and the profiles for both bundle IDs (app + widget) so they pick up
the App ID's current capabilities, and pushes the encrypted result back to `bible-planner-certs`. Re-run
the release workflow afterwards.

## The client secret and `generate_apple_client_secret.py`

Unlike Google, Apple does not give you a static OAuth client secret. The "secret" Supabase needs is a
**JWT signed with the .p8 private key (ES256)**, and Apple caps its validity at **6 months**. When it
expires, Apple sign-in on Android/desktop stops working with no code change involved — the secret must
be regenerated and pushed to Supabase again.

`supabase/generate_apple_client_secret.py` automates that. It:

1. Builds the JWT header/payload (`iss` = team ID, `sub` = Services ID, `aud` = `https://appleid.apple.com`,
   `exp` = now + 180 days).
2. Signs it with the `.p8` key using `openssl` (no third-party Python dependencies).
3. Writes the JWT to the output file path passed as argument — the private key and the secret are never
   printed to stdout.

### Automatic rotation (preferred)

The [`rotate-apple-secret`](../.github/workflows/rotate-apple-secret.yml) workflow regenerates and
pushes a fresh secret on the 1st of January, June and November (and on demand via *Run workflow*).
It runs unattended in the `apple-secret-rotation` environment (no approval; deployments restricted
to `main`) and depends on two secrets in that environment:

- `APPLE_SIGNIN_KEY_P8` — contents of the `.p8` private key
- `SUPABASE_ACCESS_TOKEN` — Supabase personal access token

> Because the environment has no required reviewer, keep repository write access restricted —
> anyone with write can reach these secrets through a workflow on `main`.

### Manual rotation (fallback)

```bash
APPLE_AUTH_KEY_P8=/path/to/AuthKey_S6T3824BDQ.p8 \
  python3 supabase/generate_apple_client_secret.py supabase/.apple_client_secret

SUPABASE_AUTH_EXTERNAL_APPLE_SECRET=$(cat supabase/.apple_client_secret) \
  supabase config push --project-ref yncazduslqvphguhwdel
```

The push reads `supabase/config.toml` (which references the secret via
`env(SUPABASE_AUTH_EXTERNAL_APPLE_SECRET)`), shows a diff, and asks for confirmation.
`supabase/.apple_client_secret` is gitignored — never commit it.

If the key is ever revoked/rotated in the Apple Developer portal, update the
`KEY_ID`/`TEAM_ID`/`CLIENT_ID` constants in the script and the `APPLE_SIGNIN_KEY_P8` secret.

## Supabase configuration

Auth provider settings live in `supabase/config.toml` and are applied with `supabase config push`
(requires `supabase login`). Notable settings:

- `[auth.external.apple]` — provider enabled, client IDs (Services ID first, then the iOS bundle ID),
  and the secret env reference.
- `[auth].additional_redirect_urls` — must contain `bibleplanner://auth-callback` (Android deep link)
  and the `http://localhost:*` / `http://127.0.0.1:*` wildcards (desktop ephemeral-port callback server).

## Troubleshooting

- **"Unsupported provider: provider is not enabled"** — the Apple provider is disabled in Supabase;
  check `[auth.external.apple] enabled = true` was pushed.
- **Browser lands on `127.0.0.1:3000` with tokens in the URL** — the OAuth `redirect_to` was rejected
  and Supabase fell back to the project Site URL; check `additional_redirect_urls`.
- **`invalid_client` from Apple** — expired or invalid client secret; regenerate it (see above).
- **iOS release fails: provisioning profile "doesn't include the Sign In with Apple capability"** — the
  match profile predates the capability being enabled on the App ID; regenerate it with
  `fastlane match appstore --force` (see [Regenerating the provisioning profiles](#regenerating-the-provisioning-profiles-fastlane-match)).
- **Android logcat shows `Failed to load session ... No entry with the key sb-...-session`** — benign
  supabase-kt noise on cold start when no session is stored yet; not an error.
