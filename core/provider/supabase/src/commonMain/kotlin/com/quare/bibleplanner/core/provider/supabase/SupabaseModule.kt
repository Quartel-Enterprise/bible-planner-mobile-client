package com.quare.bibleplanner.core.provider.supabase

import com.quare.bibleplanner.core.provider.supabase.generated.SupabaseBuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import org.koin.dsl.module

val supabaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = SupabaseBuildKonfig.SUPABASE_URL,
            supabaseKey = SupabaseBuildKonfig.SUPABASE_API_KEY,
        ) {
            install(Auth)
            install(ComposeAuth) {
                googleNativeLogin(SupabaseBuildKonfig.SUPABASE_GOOGLE_WEB_CLIENT_ID)
            }
        }
    }
}
