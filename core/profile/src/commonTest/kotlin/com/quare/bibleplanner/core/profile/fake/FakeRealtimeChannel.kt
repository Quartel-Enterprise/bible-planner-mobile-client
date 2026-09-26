package com.quare.bibleplanner.core.profile.fake

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.realtime.CallbackManager
import io.github.jan.supabase.realtime.HttpSendBuilder
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.PostgresJoinConfig
import io.github.jan.supabase.realtime.PresenceAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeSystemPayload
import io.github.jan.supabase.realtime.broadcast.BroadcastPayload
import io.github.jan.supabase.realtime.broadcast.RealtimeBroadcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

@OptIn(SupabaseInternal::class)
internal class FakeRealtimeChannel(
    override val topic: String,
    private val actions: Flow<PostgresAction>,
    private val onSubscribe: (String) -> Unit,
) : RealtimeChannel {
    override val status: StateFlow<RealtimeChannel.Status> get() = error("unused")
    override val supabaseClient: SupabaseClient get() = error("unused")
    override val realtime: Realtime get() = error("unused")
    override val callbackManager: CallbackManager get() = error("unused")
    override val logger: SupabaseLogger get() = error("unused")

    override suspend fun subscribe(blockUntilSubscribed: Boolean) {
        onSubscribe(topic)
    }

    override suspend fun updateAuth(jwt: String?) = error("unused")

    override suspend fun unsubscribe() = error("unused")

    override suspend fun broadcast(
        event: String,
        payload: BroadcastPayload,
    ) = error("unused")

    override suspend fun httpSend(
        event: String,
        payload: BroadcastPayload,
        builder: HttpSendBuilder.() -> Unit,
    ) = error("unused")

    override suspend fun track(state: JsonObject) = error("unused")

    override suspend fun untrack() = error("unused")

    override fun <T : PostgresAction> RealtimeChannel.postgresChangeFlowInternal(
        action: KClass<T>,
        schema: String,
        filter: PostgresChangeFilter.() -> Unit,
    ): Flow<T> = actions.filterIsInstance(action)

    override fun broadcastFlow(event: String): Flow<RealtimeBroadcast> = error("unused")

    override fun presenceChangeFlow(): Flow<PresenceAction> = error("unused")

    override fun systemFlow(): Flow<RealtimeSystemPayload> = error("unused")

    override fun RealtimeChannel.addPostgresChange(data: PostgresJoinConfig) = error("unused")

    override fun RealtimeChannel.removePostgresChange(data: PostgresJoinConfig) = error("unused")

    override fun updateStatus(status: RealtimeChannel.Status) = error("unused")

    override suspend fun scheduleRejoin() = error("unused")

    override fun teardown() = error("unused")
}
