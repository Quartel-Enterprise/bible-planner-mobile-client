package com.quare.bibleplanner.core.provider.supabase

import com.quare.bibleplanner.core.provider.supabase.testing.FakeRealtime
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeChannelBuilder
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SerializedRemovalRealtimeTest {
    private lateinit var delegate: GatedRemovalRealtime
    private lateinit var realtime: SerializedRemovalRealtime
    private lateinit var first: RealtimeChannel
    private lateinit var second: RealtimeChannel

    @BeforeTest
    fun setUp() {
        delegate = GatedRemovalRealtime(channels = FakeRealtime(actions = emptyFlow()))
        realtime = SerializedRemovalRealtime(delegate = delegate)
        first = realtime.channel(FIRST)
        second = realtime.channel(SECOND)
    }

    @Test
    fun `GIVEN a channel removal in progress WHEN removing another channel THEN waits for the first removal to end`() =
        runTest {
            // Given
            launch { realtime.removeChannel(first) }
            runCurrent()

            // When
            launch { realtime.removeChannel(second) }
            runCurrent()
            delegate.gate.complete(Unit)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf("start:$FIRST", "end:$FIRST", "start:$SECOND", "end:$SECOND"),
                actual = delegate.calls,
            )
        }

    @Test
    fun `GIVEN a channel removal in progress WHEN removing all channels THEN waits for the removal to end`() = runTest {
        // Given
        launch { realtime.removeChannel(first) }
        runCurrent()

        // When
        launch { realtime.removeAllChannels() }
        runCurrent()
        delegate.gate.complete(Unit)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf("start:$FIRST", "end:$FIRST", "removeAll"),
            actual = delegate.calls,
        )
    }

    @Test
    fun `GIVEN all channels already removed WHEN removing a channel THEN leaves the client alone`() = runTest {
        // Given
        realtime.removeAllChannels()

        // When
        realtime.removeChannel(first)

        // Then
        assertEquals(
            expected = listOf("removeAll"),
            actual = delegate.calls,
        )
    }

    @Test
    fun `GIVEN a channel replaced under its topic WHEN removing the old channel THEN keeps the new one`() = runTest {
        // Given
        delegate.gate.complete(Unit)
        realtime.removeChannel(first)
        val replacement = realtime.channel(FIRST)

        // When
        realtime.removeChannel(first)

        // Then
        assertEquals(
            expected = mapOf(FIRST to replacement, SECOND to second),
            actual = delegate.subscriptions,
        )
    }

    private companion object {
        const val FIRST = "first"
        const val SECOND = "second"
    }
}

private class GatedRemovalRealtime(
    private val channels: FakeRealtime,
) : Realtime by channels {
    val calls = mutableListOf<String>()
    val gate = CompletableDeferred<Unit>()

    override val subscriptions = mutableMapOf<String, RealtimeChannel>()

    override fun channel(
        channelId: String,
        builder: RealtimeChannelBuilder,
    ): RealtimeChannel = channels
        .channel(
            channelId = channelId,
            builder = builder,
        ).also { channel -> subscriptions[channel.topic] = channel }

    override suspend fun removeChannel(channel: RealtimeChannel) {
        calls += "start:${channel.topic}"
        gate.await()
        subscriptions.remove(channel.topic)
        calls += "end:${channel.topic}"
    }

    override suspend fun removeAllChannels() {
        subscriptions.clear()
        calls += "removeAll"
    }
}
