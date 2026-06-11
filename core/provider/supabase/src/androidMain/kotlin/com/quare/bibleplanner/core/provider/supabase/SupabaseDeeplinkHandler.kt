package com.quare.bibleplanner.core.provider.supabase

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

const val SUPABASE_DEEPLINK_SCHEME = "bibleplanner"
const val SUPABASE_DEEPLINK_HOST = "auth-callback"

/**
 * Imports the auth session delivered through the OAuth redirect deep link
 * (bibleplanner://auth-callback). Call it from the activity that receives the
 * VIEW intent, on both onCreate and onNewIntent.
 */
class SupabaseDeeplinkHandler(
    private val supabaseClient: SupabaseClient,
) {
    fun handle(intent: Intent) {
        supabaseClient.handleDeeplinks(intent)
    }
}
