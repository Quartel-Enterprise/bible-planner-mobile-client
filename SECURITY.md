# Security Policy

## Supported versions

Only the latest release of Bible Planner receives security fixes. A fix ships as a new release on
the [Google Play Store](https://play.google.com/store/apps/details?id=com.quare.bibleplanner) and
the [App Store](https://apps.apple.com/us/app/bible-planner-reading-plans/id6756151777), so the
answer to an issue in an older version is always to update the app.

The desktop and web apps are available for development only and have no supported releases yet.
Issues in their code are still welcome when they also affect the mobile apps.

## Reporting a vulnerability

**Do not open a public issue, discussion or pull request for a security problem.**

Report it privately through GitHub instead:

1. Open the [Security tab](https://github.com/Quartel-Enterprise/bible-planner-mobile-client/security)
   of this repository.
2. Click **Report a vulnerability**.
3. Describe the problem, the affected version and platform, and the steps to reproduce it. A proof
   of concept helps a lot.

Only the maintainers can see the report. We aim to acknowledge it within 7 days and to keep you
posted until it is fixed. Once a fix is released, we can publish an advisory and credit you, if you
want.

## Scope

In scope:

- The Android and iOS apps built from this repository.
- How the apps handle accounts, synced reading progress, notes and purchases, including access to
  another user's data through the backend the apps talk to.
- Secrets that should not be public and were committed to this repository.

Out of scope:

- Publishable client keys that ship inside the apps by design, such as the Firebase configuration
  or the Supabase anon key. They are only a vulnerability if they let someone read or change data
  they should not.
- Vulnerabilities in third-party services and libraries (Supabase, Firebase, Google Play, the App
  Store and others). Report those to their maintainers. If one affects Bible Planner in a specific
  way, tell us too.
- Attacks that need a rooted or jailbroken device, or physical access to an unlocked one.
- Denial of service, spam and social engineering.
