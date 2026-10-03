package com.quare.bibleplanner.core.provider.supabase

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

const val SUPABASE_DEEPLINK_SCHEME = "bibleplanner"
const val SUPABASE_DEEPLINK_HOST = "auth-callback"

class SupabaseDeeplinkHandler(
    private val supabaseClient: SupabaseClient,
) {
    fun handle(intent: Intent) {
        supabaseClient.handleDeeplinks(intent)
    }
}
