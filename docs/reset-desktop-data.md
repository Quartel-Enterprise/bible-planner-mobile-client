# Resetting desktop local data

The desktop (JVM) target persists user data in two places, separate from the Android/iOS apps:

| Data | Location | Defined in |
|---|---|---|
| Room database (`bible_planner.db` + `-wal`/`-shm`/`.lck`) | `java.io.tmpdir` (`$TMPDIR` on macOS/Linux) | `core/provider/room/.../DatabaseConstructor.desktop.kt` + `DatabaseUtils.PATH` |
| DataStore preferences (`prefs.preferences_pb`) | App working directory — `composeApp/` when launched via Gradle | `core/provider/data_store/.../CreateDataStore.jvm.kt` |

## When to reset

Wipe this data to start the desktop app from a clean state — for example to:

- test a Room database migration from a fresh install,
- exercise the first-launch / onboarding flow (e.g. the provisional plan start date),
- verify logout data clearing.

It is also the way to recover a pre-release dev install that hits a Room schema-identity
mismatch after an unreleased schema version is amended in place.

## How to reset

Close the desktop app first (deleting the database while the running instance holds its lock
can leave it in a bad state), then run:

```bash
scripts/reset-desktop-data.sh
```

The script removes the Room database (and its SQLite sidecars) and the DataStore file, then
reports what was deleted. On the next launch the app recreates the database at the current
schema version and seeds default preferences.
