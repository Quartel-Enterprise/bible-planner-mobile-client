package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.AuthConfig

/**
 * Configures the OAuth redirect deep link (bibleplanner://auth-callback) so browser-based
 * flows (e.g. Apple sign-in, which has no native Android support) return to the app instead
 * of the Supabase project's Site URL. The link is handled by MainActivity via
 * [SupabaseDeeplinkHandler] and must be allowlisted in the Supabase auth config
 * (see supabase/config.toml).
 */
internal actual fun AuthConfig.platformConfig() {
    scheme = SUPABASE_DEEPLINK_SCHEME
    host = SUPABASE_DEEPLINK_HOST
}
