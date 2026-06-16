# Resetting desktop local data

The desktop (JVM) target persists user data in three places, separate from the Android/iOS apps:

| Data | Location | Defined in |
|---|---|---|
| Room database (`bible_planner.db` + `-wal`/`-shm`/`.lck`) | `java.io.tmpdir` (`$TMPDIR` on macOS/Linux) | `core/provider/room/.../DatabaseConstructor.desktop.kt` + `DatabaseUtils.PATH` |
| DataStore preferences (`prefs.preferences_pb`) | App working directory — `composeApp/` when launched via Gradle | `core/provider/data_store/.../CreateDataStore.jvm.kt` |
| Supabase auth session (`sb-<ref>-supabase-co-session`) | JVM user-root preferences (`~/Library/Preferences/com.apple.java.util.prefs.plist` on macOS) | supabase-kt's default `SettingsSessionManager` |

The Supabase session is the easy one to miss: it lives in `java.util.prefs.Preferences`, not in
the Room database or DataStore, so leaving it behind keeps the account logged in after a reset —
and a stale/expired token there breaks authenticated storage calls such as the Bible-version list.

## When to reset

Wipe this data to start the desktop app from a clean state — for example to:

- test a Room database migration from a fresh install,
- exercise the first-launch / onboarding flow (e.g. the provisional plan start date),
- verify logout data clearing.

It is also the way to recover a pre-release dev install that hits a Room schema-identity
mismatch after an unreleased schema version is amended in place.

## How to reset

```bash
scripts/reset-desktop-data.sh
```

If the desktop app is running, the script detects it (by its `com.quare.bibleplanner.MainKt`
main class) and asks whether to close it and continue or to cancel — deleting the database while
the running instance holds its lock can leave it in a bad state. In a non-interactive shell it
refuses instead of prompting.

The script then removes the Room database (and its SQLite sidecars), the DataStore file, and the
Supabase auth session, reporting what was deleted. Clearing the session needs a JDK on `PATH` or
via `JAVA_HOME` (it removes only the `sb-*`/`supabase*` keys through the Preferences API, leaving
sibling IDE preferences intact); without one it warns and leaves the account logged in. On the
next launch the app recreates the database at the current schema version, seeds default
preferences, and starts logged out.
