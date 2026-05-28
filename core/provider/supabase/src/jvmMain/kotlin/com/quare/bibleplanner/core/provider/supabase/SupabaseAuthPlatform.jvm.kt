package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.AuthConfig

internal actual fun AuthConfig.platformConfig() {
    httpCallbackConfig {
        // Leave httpPort at its default (0 = OS-assigned ephemeral port).
        //
        // supabase-kt 3.6.0's HTTP callback server shuts down fire-and-forget after a
        // successful login: signInWith() returns BEFORE the socket is actually released,
        // and the Ktor CIO engine doesn't set SO_REUSEADDR. Pinning the port therefore
        // breaks the second consecutive login in the same JVM with
        // `BindException: Address already in use`.
        //
        // The Supabase Dashboard redirect-URL allowlist must include `http://localhost:**`
        // so any chosen port is accepted.
        htmlTitle = "Bible Planner"
        // `redirectHtml` is updated per-login by `JvmGoogleSignInStarter`, so it can reflect
        // the user's current in-app theme and language. The default supabase-kt page is only
        // shown if the user somehow triggers OAuth before any login attempt completes.
    }
}
