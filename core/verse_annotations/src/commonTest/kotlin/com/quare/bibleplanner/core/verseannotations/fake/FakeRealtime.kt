package com.quare.bibleplanner.core.verseannotations.fake

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeChannelBuilder
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocket
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(SupabaseInternal::class)
internal class FakeRealtime(
    private val actions: Flow<PostgresAction>,
) : Realtime {
    val createdChannelIds = mutableListOf<String>()
    val subscribedChannelIds = mutableListOf<String>()
    val removedChannelIds = mutableListOf<String>()

    override val status: MutableStateFlow<Realtime.Status> = MutableStateFlow(Realtime.Status.DISCONNECTED)
    override val subscriptions: Map<String, RealtimeChannel> get() = error("unused")
    override val websocket: RealtimeWebsocket get() = error("unused")
    override val config: Realtime.Config get() = error("unused")
    override val supabaseClient: SupabaseClient get() = error("unused")
    override val apiVersion: Int get() = error("unused")
    override val pluginKey: String get() = error("unused")
    override val logger: SupabaseLogger get() = error("unused")
    override val serializer: SupabaseSerializer get() = error("unused")

    override suspend fun parseErrorResponse(response: HttpResponse): RestException = error("unused")

    override suspend fun connect() = error("unused")

    override fun disconnect() = error("unused")

    override suspend fun removeChannel(channel: RealtimeChannel) {
        removedChannelIds += channel.topic
    }

    override fun addChannel(channel: RealtimeChannel) = error("unused")

    override suspend fun removeAllChannels() = error("unused")

    override suspend fun block() = error("unused")

    override suspend fun send(message: RealtimeMessage) = error("unused")

    override suspend fun send(message: ByteArray) = error("unused")

    override suspend fun setAuth(token: String?) = error("unused")

    override fun channel(
        channelId: String,
        builder: RealtimeChannelBuilder,
    ): RealtimeChannel {
        createdChannelIds += channelId
        return FakeRealtimeChannel(
            topic = channelId,
            actions = actions,
            onSubscribe = subscribedChannelIds::add,
        )
    }
}
