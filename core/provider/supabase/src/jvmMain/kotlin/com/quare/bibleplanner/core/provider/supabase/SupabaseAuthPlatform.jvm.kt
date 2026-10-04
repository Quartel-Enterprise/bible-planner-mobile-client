package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.AuthConfig

internal actual fun AuthConfig.platformConfig() {
    httpCallbackConfig {
        /*
         * Why: httpPort stays 0 (ephemeral): supabase-kt 3.6.0 releases the callback socket after
         * signInWith() returns and CIO lacks SO_REUSEADDR, so a fixed port breaks the 2nd login
         * (BindException). The Supabase redirect allowlist must include http://localhost:**.
         */
        htmlTitle = "Bible Planner"
        /*
         * Why: redirectHtml is not set here; JvmSignInStarter updates it per login to match the current
         * theme and language.
         */
    }
}
