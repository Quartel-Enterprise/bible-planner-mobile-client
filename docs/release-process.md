# Release Process

This document explains how Bible Planner is released to the **Google Play Store** and the
**Apple App Store** through the automated pipeline defined in
[`.github/workflows/release.yml`](../.github/workflows/release.yml).

## Overview

A release is a single manual action: you run the **release** workflow from the GitHub Actions
tab. The pipeline resolves the version, pauses for your approval, builds and uploads the mobile
apps, packages the desktop installers, and then tags the release and merges the version bump
back into `main`.

Nothing is created or published before you approve the run, and all credentials live in the
`Production` GitHub Environment — only jobs that pass the approval gate can read them.

## Pipeline at a glance

```mermaid
flowchart TD
    A([Trigger: Run workflow]) --> B[plan<br/>resolve version]
    B --> G{{Production gate<br/>manual approval}}
    G -->|rejected| X([Run stops — nothing built])
    G -->|approved| D[android<br/>build AAB + upload to Play]
    G -->|approved| E[ios<br/>build IPA + submit to App Store]
    G -->|approved| H[desktop<br/>build dmg + msi + deb + rpm]
    D --> F[finalize]
    E --> F
    H --> F
    F --> F1[Create release/X.Y.Z branch + version bump]
    F1 --> F2[Open + squash-merge the merge-back PR into main]
    F2 --> F3([Publish GitHub Release X.Y.Z<br/>with the installers attached])
```

| Job | Runner | Gated | What it does |
|-----|--------|-------|--------------|
| `plan` | ubuntu | no | Resolves the version and shows it in the run summary |
| `android-build` | ubuntu | yes | Builds the signed AAB and saves it as a build artifact |
| `android-upload` | ubuntu | yes | Uploads the AAB to Google Play |
| `ios-build` | macOS | yes | Builds the signed IPA and saves it as a build artifact |
| `ios-upload` | macOS | yes | Uploads the IPA to App Store Connect and submits it for review |
| `desktop-build` | macOS + windows + ubuntu | yes | Builds the `.dmg`, `.msi`, `.deb` and `.rpm` installers, one runner per OS |
| `finalize` | ubuntu | no | Branch + version bump, merge-back PR, GitHub Release with the installers attached |

Android and iOS split build and upload into separate jobs, so a failed upload can be retried
(re-run just the `*-upload` job) without paying for another build. Desktop has no store to
upload to: the installers are attached to the GitHub release by `finalize`. They are unsigned,
so macOS Gatekeeper and Windows SmartScreen warn on first launch.

## Triggering a release

1. Make sure the in-app release notes JSON has an entry for the upcoming version
   (see [Release notes](#release-notes-whats-new)).
2. Open **GitHub → Actions → release → Run workflow**.
3. Fill in the inputs (all optional):
   - **version** — leave blank to auto-resolve, or type an explicit `X.Y.Z`.
   - **platforms** — `all` (default), `mobile` (android + ios), `android`, `ios`, or `desktop`.
   - **track** — Android Play Store track: `production` (default), `beta`, `alpha`, `internal`.
   - **complete_android_release** — `true` (default) rolls the Android release out; `false`
     uploads it to the track as a draft you release manually from the Play Console.
   - **submit_ios_for_review** — `true` (default) submits the iOS build for App Store review;
     `false` only uploads it to App Store Connect / TestFlight (use it for test runs).
   - **prerelease** — `false` (default). `true` ships a beta that never reaches the public:
     the version is named `X.Y.Z-beta-N`, Android goes to the Play **internal** track, iOS to
     TestFlight without review, desktop is skipped and the GitHub Release is marked as a
     **pre-release**. `track` and `submit_ios_for_review` are ignored (forced to `internal` /
     off), and `platforms` must be `all` or `mobile`.

Alternatively, run `./scripts/release/release.sh` from the terminal — it asks for the same inputs
as a menu (with the same defaults) and prints the link to the dispatched run. For a full
production release on every platform with no prompts, run `./scripts/release/release-prod.sh`;
for a pre-release beta with no prompts, run `./scripts/release/release-beta.sh`.
4. Click **Run workflow**.
5. The `plan` job runs and prints the resolved version in the run summary. The run then pauses
   on the **Production** environment.
6. Open the run, review the planned version, and **approve** (or **reject** and start over).
7. The build, upload and finalize steps run automatically.

### Example — full production release of 1.14.0

```text
version:                  (blank — auto-resolved to 1.14.0 from the release notes JSON)
platforms:                all
track:                    production
complete_android_release: true
submit_ios_for_review:    true
```

### Example — safe test run (no production impact)

```text
version:                  (blank)
platforms:                all
track:                    internal
complete_android_release: false
submit_ios_for_review:    false
```

Android uploads to the internal testing track as a draft and iOS lands in App Store Connect /
TestFlight without being submitted for review. The `finalize` job only runs for
`platforms = all|mobile` **and** `track = production` (or a pre-release), so a test run creates
no tag, GitHub Release or merge-back PR.

### Example — pre-release beta of 2.4.0

```text
version:                  (blank — auto-resolved, becomes 2.4.0-beta-1)
platforms:                mobile
track:                    (ignored — forced to internal)
complete_android_release: true
submit_ios_for_review:    (ignored — forced off)
prerelease:               true
```

Unlike a test run, a pre-release **does** finalize: it tags `2.4.0-beta-N`, squash-merges a
versionCode-only bump back into `main` (the `versionName` in `version.xcconfig` stays at the
last stable until the real release) and publishes a GitHub Release marked as a **pre-release**,
with no installers attached. The beta number `N` is computed automatically from the existing
`2.4.0-beta-*` tags. The `beta-N` suffix appears on the Android `versionName`, the tag and the
GitHub Release — Apple rejects suffixed version strings, so the iOS build keeps the plain
`X.Y.Z` and is distinguished in TestFlight by its build number (the `versionCode`). The store
"What's New" still comes from the plain `X.Y.Z` entry in the release notes JSON.

## How the version is resolved

The `plan` job runs [`scripts/suggest-version.sh`](../scripts/suggest-version.sh), which picks
the version in this order:

```mermaid
flowchart TD
    A([Resolve version]) --> B{version input<br/>provided?}
    B -->|yes| C[Use the input]
    B -->|no| D{Release notes JSON<br/>ahead of the released version?}
    D -->|yes| E[Use the JSON's highest version key]
    D -->|no| F[Infer from commits since the last release:<br/>any feature or feat commit gives a minor bump<br/>otherwise a patch bump]
```

- The **`versionCode`** (Android) / **`CURRENT_PROJECT_VERSION`** (iOS) is always the current
  value **+ 1** — including for pre-releases, which is why each beta merges a versionCode-only
  bump back into `main`.
- The release notes JSON is the preferred source: its newest key is the version the team has
  been accumulating notes for, so the version and the "What's New" come from the same place.
- For a pre-release, the resolved `X.Y.Z` additionally gets a `-beta-N` suffix, where `N` is
  one past the highest existing `X.Y.Z-beta-*` tag.

## Release notes ("What's New")

Release notes live in three JSON files, keyed by version:

```
feature/release_notes/src/commonMain/composeResources/files/release_notes/{en,pt,es}.json
```

```json
{
  "1.14.0": [
    "You can now change the app language from the 'More' screen.",
    "Fixed extra whitespace below the search bar."
  ]
}
```

Keep them updated during the development cycle with the `release-notes-updater` skill. At
release time the pipeline reads the entry for the version being shipped and publishes it as the
store "What's New":

- **Google Play** — written as changelog files for every listing locale (`en-US`, `pt-BR`,
  `es-419`, `es-ES`, `es-US`).
- **App Store** — the listing's locales are discovered at runtime via the App Store Connect API,
  and the notes are matched by language.

If no JSON entry exists for the version, the build still ships — the store "What's New" is just
left unchanged.

## Approval gate

The `android` and `ios` jobs target the `Production` GitHub Environment, which has **required
reviewers**. The run pauses before those jobs until a reviewer approves it. Because `plan` has
already run, the reviewer can see the resolved version in the run summary before approving.

To use a different version than the one resolved, **reject** the run and start a new one with
the `version` input set.

## After a release

Once the store uploads and the desktop builds succeed, `finalize`:

1. Creates the `release/X.Y.Z` branch with the version bump commit (Android, iOS and Desktop —
   see [`scripts/bump-version.sh`](../scripts/bump-version.sh)).
2. Opens a `chore: merge back X.Y.Z into main` pull request and squash-merges it into `main`.
3. Publishes a **GitHub Release** `X.Y.Z` with auto-generated notes and the `.dmg`, `.msi`,
   `.deb` and `.rpm` installers attached as assets.

For a pre-release the branch is `release/X.Y.Z-beta-N`, the bump commit only advances the
`versionCode`, and the GitHub Release `X.Y.Z-beta-N` carries the **pre-release** badge with no
installers. Pre-releases are never marked "Latest", so anything that consumes the latest
release (like `scripts/install-latest-release-android.sh`) keeps seeing the last stable.

## If a store rejects the build

Apple/Google review is still a human gate and can reject a build. If that happens, fix the code
and ship it as the next version — the rejected version's GitHub Release/tag can simply be
deleted. The version code is already consumed, which is fine: version codes only need to keep
increasing.

## Manual version bump (local)

To bump versions outside the pipeline (Android, iOS and Desktop at once):

```bash
./scripts/bump-version.sh 1.14.0 23
#                          │      └─ versionCode
#                          └──────── versionName
```

## Configuration reference

| Item | Value |
|------|-------|
| Workflow | `.github/workflows/release.yml` |
| Fastlane lanes | `fastlane/Fastfile` (`android release`, `ios release`) |
| iOS certificates | fastlane match — private repo `bible-planner-certs` |
| Approval gate | `Production` GitHub Environment (required reviewers) |

All secrets are stored in the `Production` environment, never committed:

`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
`ANDROID_KEY_PASSWORD`, `PLAY_STORE_SERVICE_ACCOUNT_JSON`, `GOOGLE_SERVICES_JSON`,
`GOOGLE_SERVICE_INFO_PLIST`, `APP_STORE_CONNECT_API_KEY_ID`,
`APP_STORE_CONNECT_API_ISSUER_ID`, `APP_STORE_CONNECT_API_KEY_P8`, `MATCH_PASSWORD`,
`MATCH_SSH_PRIVATE_KEY`, `LOCAL_PROPERTIES`.

`LOCAL_PROPERTIES` holds the contents of `local.properties` (minus `sdk.dir`) — the
build-time values consumed by BuildKonfig (Supabase, RevenueCat, donation addresses, the
GitHub token and the desktop analytics keys `GA_MEASUREMENT_ID` / `GA_MEASUREMENT_API_SECRET`).
The workflow writes it to `local.properties` before building, since that file
is git-ignored and would otherwise be missing on the runner.
