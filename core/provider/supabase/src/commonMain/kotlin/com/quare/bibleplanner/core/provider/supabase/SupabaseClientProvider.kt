package com.quare.bibleplanner.core.provider.supabase

import com.quare.bibleplanner.core.provider.supabase.generated.SupabaseBuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val realtimeHeartbeatInterval: Duration = 10.seconds

private val realtimeReconnectDelay: Duration = 3.seconds

internal fun getSupabaseClient(monitoredSessionManager: SessionManager): SupabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseBuildKonfig.SUPABASE_URL,
    supabaseKey = SupabaseBuildKonfig.SUPABASE_API_KEY,
) {
    httpEngine = createPlatformHttpEngine()
    defaultLoggingFactory = ::KermitSupabaseLoggingProcessor
    install(Auth) {
        platformConfig()
        sessionManager = monitoredSessionManager
    }
    install(ComposeAuth) {
        googleNativeLogin(SupabaseBuildKonfig.SUPABASE_GOOGLE_WEB_CLIENT_ID)
        appleNativeLogin()
    }
    install(Storage)
    install(Postgrest)
    install(Functions)
    install(Realtime) {
        heartbeatInterval = realtimeHeartbeatInterval
        reconnectDelay = realtimeReconnectDelay
    }
}
