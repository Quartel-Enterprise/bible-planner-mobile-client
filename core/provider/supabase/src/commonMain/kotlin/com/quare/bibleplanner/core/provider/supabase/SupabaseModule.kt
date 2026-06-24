package com.quare.bibleplanner.core.provider.supabase

import com.quare.bibleplanner.core.provider.supabase.generated.SupabaseBuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.stateIn
import org.koin.dsl.module
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val SUPABASE_BUCKET_NAME = "content"

// Tighter than the SDK default (15s) to reduce idle NAT/proxy "Connection reset" drops.
private val realtimeHeartbeatInterval: Duration = 10.seconds

// Shorter than the SDK default (7s) so live updates resume sooner after a drop.
private val realtimeReconnectDelay: Duration = 3.seconds

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
            install(Realtime) {
                heartbeatInterval = realtimeHeartbeatInterval
                reconnectDelay = realtimeReconnectDelay
            }
        }
    }
    single<Auth> { get<SupabaseClient>().auth }
    single<StateFlow<SessionStatus>> { get<Auth>().sessionStatus.ignoringTransientInitializing() }
    single<Realtime> { get<SupabaseClient>().realtime }
    single<BucketApi> {
        get<SupabaseClient>().storage.from(SUPABASE_BUCKET_NAME)
    }
}

private fun StateFlow<SessionStatus>.ignoringTransientInitializing(): StateFlow<SessionStatus> =
    runningReduce { resolved, next ->
        if (next is SessionStatus.Initializing) resolved else next
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = value,
    )
