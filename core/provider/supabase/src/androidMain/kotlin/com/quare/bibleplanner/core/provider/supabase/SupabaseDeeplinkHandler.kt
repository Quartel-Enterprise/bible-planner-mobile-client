package com.quare.bibleplanner.core.provider.supabase

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

const val SUPABASE_DEEPLINK_SCHEME = "bibleplanner"
const val SUPABASE_DEEPLINK_HOST = "auth-callback"

/*
 * Why: must be called from the activity receiving the VIEW intent in both onCreate and
 * onNewIntent, or a redirect to an already-running activity drops the OAuth session.
 */
class SupabaseDeeplinkHandler(
    private val supabaseClient: SupabaseClient,
) {
    fun handle(intent: Intent) {
        supabaseClient.handleDeeplinks(intent)
    }
}
