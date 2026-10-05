---
name: project-agent-cli
description: ":tools:agent-cli (Oct/26, branch chore/agent-cli) drives real ViewModels headlessly on the JVM via scripts/agent-cli.sh; non-obvious isolation and packaging traps"
metadata:
  node_type: memory
  type: project
  originSessionId: c1b998eb-e24a-40b3-80c8-4b4b2962688c
  modified: 2026-10-04T17:00:22.632Z
---

`:tools:agent-cli` (branch `chore/agent-cli`, started 2026-10-04 after discussing Shopify's "back to native" article) runs the real Koin graph/Room/ViewModels on the JVM, no window. `scripts/agent-cli.sh start|<command>|stop` keeps a warm daemon (loopback HTTP); docs in docs/agent-cli.md.

Non-obvious bits:
- JVM storage isolation needs BOTH `user.home` (Room/DataStore via resolveAppDataDirectory) AND java.util.prefs (Supabase SettingsSessionManager, GA4 client id, RevenueCat anon id) — on macOS prefs is one shared plist, so the CLI would sign out the dev's desktop app.
- JDK's FileSystemPreferencesFactory throws UnsatisfiedLinkError (chmod) on macOS → the CLI ships its own FilePreferencesFactory.
- `installDist` is impossible: :core:chapter_study and :feature:chapter_study both build `chapter_study-jvm.jar`; the module writes java+classpath files (`agentCliLauncher` task) instead.
- Compose Resources getString needs Skiko natives → `compose.desktop.currentOs`.
- Route→ViewModel mapping lives in entry composables; `AppScreens` lists the exceptions and AppScreensTest fails on a route that maps to nothing.

**Why:** lets agents verify ViewModel/use-case changes in ms instead of simulator screenshots.
**How to apply:** prefer the agent CLI for logic checks; UI/layout still needs UI tests or a device. Related: [[project-headless-jvm-tests]].
