package com.quare.bibleplanner.feature.login.presentation

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.createSupabaseClient

internal fun createTestSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://test.supabase.co",
    supabaseKey = "anon-key",
) {
    install(Auth) {
        sessionManager = MemorySessionManager()
        codeVerifierCache = MemoryCodeVerifierCache()
        autoLoadFromStorage = false
        alwaysAutoRefresh = false
        enableLifecycleCallbacks = false
    }
    install(ComposeAuth)
}
