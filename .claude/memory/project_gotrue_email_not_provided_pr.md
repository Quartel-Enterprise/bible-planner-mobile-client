---
name: project-gotrue-email-not-provided-pr
description: "Pending upstream GoTrue PR wires email_address_not_provided code; once merged, switch the login error mapper from message-match to code-match."
metadata: 
  node_type: memory
  type: project
  originSessionId: 5ad294f6-bd67-4fd0-8cb8-4690bb14b40f
---

On 2026-06-17 we opened an upstream contribution to GoTrue (Supabase Auth) so that a missing email from an external provider returns the dedicated `email_address_not_provided` error code instead of a generic 500 / `unexpected_failure`:
- Issue: https://github.com/supabase/auth/issues/2583
- PR: https://github.com/supabase/auth/pull/2584 (from fork `PierreVieira/auth`, branch `fix/email-address-not-provided-error-code`; local clone at `/Users/pierrevieira/StudioProjects/auth`)

**Why this matters here:** the app's `ThrowableToLoginErrorMapper` currently detects the "Apple sign-in without email" case by **substring-matching** the GoTrue message `"Error getting user email from external provider"` (fragile) to map it to `LoginError.EMAIL_REQUIRED`.

**How to apply once the PR merges and ships:** supabase-kt has no `AuthErrorCode.EmailAddressNotProvided` entry yet — add it there (separate small PR), then change the mapper to match on the error code instead of the message string. Note: the change only affects the **web OAuth callback** path (Android/desktop); the native `id_token` (iOS Apple) path does not reject missing email at all today — raised as a question in the issue. Server-side enforcement still relies on the Supabase dashboard "Allow users without an email" = OFF (Apple provider).
