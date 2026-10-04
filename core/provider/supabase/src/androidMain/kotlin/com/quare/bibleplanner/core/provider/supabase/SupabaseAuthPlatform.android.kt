package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.AuthConfig

/*
 * Why: browser OAuth flows (Apple has no native Android sign-in) must return via this deep link,
 * not the Site URL; it must be allowlisted in supabase/config.toml.
 */
internal actual fun AuthConfig.platformConfig() {
    scheme = SUPABASE_DEEPLINK_SCHEME
    host = SUPABASE_DEEPLINK_HOST
}
