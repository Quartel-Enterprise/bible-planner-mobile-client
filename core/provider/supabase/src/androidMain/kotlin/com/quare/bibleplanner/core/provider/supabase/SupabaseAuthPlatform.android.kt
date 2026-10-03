package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.AuthConfig

internal actual fun AuthConfig.platformConfig() {
    scheme = SUPABASE_DEEPLINK_SCHEME
    host = SUPABASE_DEEPLINK_HOST
}
