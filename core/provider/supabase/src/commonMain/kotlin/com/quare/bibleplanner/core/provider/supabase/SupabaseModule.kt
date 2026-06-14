package com.quare.bibleplanner.core.provider.supabase

import com.quare.bibleplanner.core.provider.supabase.generated.SupabaseBuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.koin.dsl.module

private const val SUPABASE_BUCKET_NAME = "content"

val supabaseModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = SupabaseBuildKonfig.SUPABASE_URL,
            supabaseKey = SupabaseBuildKonfig.SUPABASE_API_KEY,
        ) {
            httpEngine = createPlatformHttpEngine()
            install(Auth) {
                platformConfig()
            }
            install(ComposeAuth) {
                googleNativeLogin(SupabaseBuildKonfig.SUPABASE_GOOGLE_WEB_CLIENT_ID)
                appleNativeLogin()
            }
            install(Storage)
            install(Postgrest)
            install(Realtime)
        }
    }
    single<Auth> { get<SupabaseClient>().auth }
    single<Realtime> { get<SupabaseClient>().realtime }
    single<BucketApi> {
        get<SupabaseClient>().storage.from(SUPABASE_BUCKET_NAME)
    }
}
