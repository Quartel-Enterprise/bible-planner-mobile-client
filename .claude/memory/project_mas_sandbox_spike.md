---
name: mas-sandbox-spike
description: Mac App Store distribution for the desktop app (#204) — proven viable end to end; sandbox pitfalls (quarantined JNI dylib, network.server for OAuth), packaging traps and signing setup.
metadata:
  node_type: memory
  type: project
---

Issue #204. The JVM desktop app **can** ship through the Mac App Store: build `2.2.0 (39)` was accepted by App Store Connect and installed from TestFlight on macOS 26 with working Google/Apple sign-in. Everything below is what it took, and every item cost real debugging time.

## Sandbox runtime traps

**Room hung forever on the first writer connection.** `BundledSQLiteDriver` extracts `libsqliteJni.dylib` at runtime and `dlopen`s it. Files a sandboxed process creates get `com.apple.quarantine` automatically, and `dlopen` of quarantined code blocks forever — Gatekeeper with no UI, no exception, no `sandboxd` deny, just a suspended coroutine and `SQLITE_BUSY` for everyone waiting. Proven with a java agent: quarantined loads hang, unquarantined ones succeed regardless of signature (ad-hoc or dev cert). Fix: ship the dylib inside the app image (from the `sqlite-bundled` jar, `natives/osx_arm64/`) and set `-Djava.library.path=$APPDIR/resources:$APPDIR`, because `NativeLibraryLoader` tries `System.loadLibrary` *before* extracting. Alternative: the `androidx.sqlite.driver.bundled.path`/`.name` properties. Non-sandboxed builds are unaffected, but the fix is harmless there.

**OAuth login spun forever.** supabase-kt's `httpCallbackConfig` runs a localhost HTTP server to catch the redirect, which needs `com.apple.security.network.server`. `network.client` only covers outbound connections. No error surfaces — the socket simply never receives the connection. This is why `:desktopApp:run` works and the packaged build doesn't.

## Packaging traps

- Entitlements plists **cannot contain XML comments** — `AMFIUnserializeXML` fails to parse them.
- `JAVA_HOME` and `-Dorg.gradle.java.home` never reach jpackage. Use the Compose `javaHome` DSL property (wired to `DESKTOP_PACKAGING_JAVA_HOME`). Packaging needs a self-contained JDK: Homebrew's links `libfontmanager` against `/opt/homebrew`, unreachable from the sandbox.
- `createRuntimeImage` stays UP-TO-DATE when the JDK changes, so the `.pkg` silently wraps the old runtime. Delete `desktopApp/build/compose/tmp/main/runtime` when switching JDKs.
- jpackage's `--name` comes from the **global** `packageName`; `JvmApplicationContext` reads only `nativeDistributions.packageName`, and `macOS.packageName` maps solely to `--mac-package-name` (menu bar). Changing the global name requires pinning `macOS.bundleID` (otherwise it is derived from the new name and breaks the already-released `.dmg`) and setting `linux.packageName` to a lowercase, space-free value.
- Provisioning profiles must be named `embedded.provisionprofile` — the plugin copies them keeping the file name and `codesign` rejects anything else. Hence `desktopApp/macos/app/` and `desktopApp/macos/runtime/`.
- The jpackage runtime is a nested bundle with its own identifier: `com.oracle.java.<app bundle id>`.

## App Store Connect rejections, and what each one needs

| Error | Cause and fix |
|---|---|
| 409 | `LSMinimumSystemVersion` defaults to 10.13, where Apple demands a universal binary. Declaring 12.0 permits arm64-only — which is forced anyway, since jlink emits only the host arch and androidx.sqlite has no macOS Intel native. |
| 90886 | The app bundle needs `application-identifier` + `team-identifier` signed in; Xcode injects these from the profile, jpackage does not. |
| 90885 / 90286 | Those same keys must **not** appear in the runtime entitlements: `jspawnhelper` is not a bundle, cannot carry a profile, and its app-id would be the runtime's synthetic one, which Apple rejects. |
| -19232 | `CFBundleVersion` must be higher than any previously uploaded build — it tracks `versionCode`. |

Without `ITSAppUsesNonExemptEncryption=false` in the Info.plist (via `infoPlist { extraKeysRawXml }`), App Store Connect holds the build for export compliance and its build page **silently hides the Add Group / Add Testers controls** — a confusing symptom, since the equivalent iOS build page shows them. The encryption questionnaire (Distribution > macOS > Build > Manage) was answered "None of the algorithms mentioned above" on 2026-07-29, on the reading that the exemption follows the *use* (HTTPS only) rather than whose TLS implementation ships. Apple's own pages disagree here: the App Store Connect reference implies a bundled JRE would need a French declaration, while the `ITSAppUsesNonExemptEncryption` documentation says "including any third-party libraries it links against". If that call is ever revisited, the Info.plist key has to go.

The TestFlight group is set to "Build Distribution: Automatic for Xcode Builds", so Transporter uploads are not auto-distributed — though once compliance was answered, the group attached itself anyway.

## Signing setup

Compose's `MacSigner` only accepts the `Developer ID Application:` and `3rd Party Mac Developer Application:` prefixes — never `Apple Distribution`. `MAC_APP_STORE_SIGNING_IDENTITY` must be the bare name (`Antônio Vieira (TTV2A365LG)`); the plugin prepends the prefix.

`match` refuses to create the classic certificate because `certs/distribution/` already holds the iOS `Apple Distribution` one, so it was created with `fastlane run cert generate_apple_certs:false platform:macos` (cert `3JU347K9BN`) and the profiles with `sigh force cert_id:...`. The certificate was later imported into the match repo with `fastlane match import --type appstore --platform macos` (prompts, in order: `.cer`, `.p12`, empty to skip the profile). `match import` requires a **real TTY** (`verify_interactive!`) — piped stdin fails.

`certs/distribution/` now holds two certificates. The iOS lane selects by name (`code_sign_identity: "Apple Distribution"`), and a readonly `match appstore --platform ios` run confirmed it still resolves correctly.

**Security note:** `fastlane cert`/`sigh` drop `.cer`/`.p12`/`.certSigningRequest` into the working directory, and `cert`'s `.p12` is an unencrypted PEM RSA private key. `.gitignore` now covers all three patterns. One did land untracked in the repo root and was removed; `git log -S` confirmed it never reached history.

Also: fastlane needs `LANG`/`LC_ALL=en_US.UTF-8` in non-interactive shells, or the accent in "Antônio" breaks a US-ASCII regex.

## Still open

The macOS platform was added to the App Store Connect record on 2026-07-29 (universal purchase). Its version entry starts at "1.0" and must be aligned before submitting. Automating macOS uploads in CI needs the two provisioning profiles imported into match, a `MAC_APP_STORE_SIGNING_IDENTITY` secret, and a job mirroring `ios-upload`.

The real blocker is not technical: the build carries the RevenueCat web checkout, which guideline 3.1.1 disallows on the Brazilian storefront, so this only enables TestFlight. Public release needs a StoreKit bridge (see [[project-revenuecat-identity]]) or a paywall disabled in the MAS variant.
