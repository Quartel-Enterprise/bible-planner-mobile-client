package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.stateIn
import org.koin.dsl.module

private const val SUPABASE_BUCKET_NAME = "content"

val supabaseModule = module {
    single<SupabaseClient> { getSupabaseClient() }
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
