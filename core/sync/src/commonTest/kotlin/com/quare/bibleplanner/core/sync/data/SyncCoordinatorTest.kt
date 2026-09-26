package com.quare.bibleplanner.core.sync.data

import com.quare.bibleplanner.core.model.AppForegroundStateHolder
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.RealtimeChannelBuilder
import io.github.jan.supabase.realtime.RealtimeMessage
import io.github.jan.supabase.realtime.websocket.RealtimeWebsocket
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class SyncCoordinatorTest {
    private lateinit var userId: MutableStateFlow<String?>
    private lateinit var realtime: StatusOnlyRealtime
    private lateinit var foregroundStateHolder: AppForegroundStateHolder
    private lateinit var first: RecordingSynchronizer
    private lateinit var second: RecordingSynchronizer
    private lateinit var trackedEvents: MutableList<String>

    @Test
    fun `GIVEN a signed in user WHEN syncing THEN seeds and starts the push loop of every dataset`() = runTest {
        // Given
        prepareScenario(initialUserId = USER_ID)

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = listOf("seed:$NOW", "runPushLoop"),
            actual = first.calls,
        )
        assertEquals(
            expected = listOf("seed:$NOW", "runPushLoop"),
            actual = second.calls,
        )
    }

    @Test
    fun `GIVEN nobody is signed in WHEN syncing THEN starts nothing`() = runTest {
        // Given
        prepareScenario(initialUserId = null)

        // When
        runCurrent()

        // Then
        assertTrue(first.calls.isEmpty())
    }

    @Test
    fun `GIVEN the app in the foreground WHEN syncing THEN observes realtime changes`() = runTest {
        // Given
        prepareScenario(initialUserId = USER_ID)

        // When
        foregroundStateHolder.onForegrounded()
        runCurrent()

        // Then
        assertEquals(
            expected = listOf("seed:$NOW", "runPushLoop", "observeRealtime"),
            actual = first.calls,
        )
    }

    @Test
    fun `GIVEN the app goes to the background WHEN it comes back THEN observes realtime again`() = runTest {
        // Given
        prepareScenario(initialUserId = USER_ID)
        foregroundStateHolder.onForegrounded()
        runCurrent()
        foregroundStateHolder.onBackgrounded()
        runCurrent()

        // When
        foregroundStateHolder.onForegrounded()
        runCurrent()

        // Then
        assertEquals(
            expected = 2,
            actual = first.calls.count { it == "observeRealtime" },
        )
    }

    @Test
    fun `GIVEN realtime connects WHEN syncing THEN every dataset pulls its snapshot`() = runTest {
        // Given
        prepareScenario(initialUserId = USER_ID)
        runCurrent()

        // When
        realtime.status.value = Realtime.Status.CONNECTED
        runCurrent()

        // Then
        assertTrue("pullSnapshot" in first.calls)
        assertTrue("pullSnapshot" in second.calls)
        assertEquals(
            expected = listOf(AnalyticsEventNames.SYNC_COMPLETED),
            actual = trackedEvents,
        )
    }

    @Test
    fun `GIVEN a signed in user WHEN another account signs in THEN wipes the local data before seeding again`() =
        runTest {
            // Given
            prepareScenario(initialUserId = USER_ID)
            runCurrent()

            // When
            userId.value = "another-user"
            runCurrent()

            // Then
            assertEquals(
                expected = listOf("seed:$NOW", "runPushLoop", "clearLocal", "seed:$NOW", "runPushLoop"),
                actual = first.calls,
            )
        }

    @Test
    fun `GIVEN a sign out WHEN another account signs in THEN keeps the local data`() = runTest {
        // Given
        prepareScenario(initialUserId = USER_ID)
        runCurrent()
        userId.value = null
        runCurrent()

        // When
        userId.value = "another-user"
        runCurrent()

        // Then
        assertTrue("clearLocal" !in first.calls)
    }

    private fun TestScope.prepareScenario(initialUserId: String?) {
        userId = MutableStateFlow(initialUserId)
        realtime = StatusOnlyRealtime()
        foregroundStateHolder = AppForegroundStateHolder()
        first = RecordingSynchronizer()
        second = RecordingSynchronizer()
        trackedEvents = mutableListOf()
        val synchronizers = listOf(first, second)
        val coordinator = SyncCoordinator(
            observeAuthenticatedUserId = { userId },
            synchronizers = synchronizers,
            snapshotPuller = SnapshotPuller(
                synchronizers = synchronizers,
                trackEvent = { name, _ -> trackedEvents += name },
            ),
            realtime = realtime,
            currentTimestampProvider = { NOW },
            appForegroundStateHolder = foregroundStateHolder,
        )
        backgroundScope.launch { coordinator() }
    }

    private companion object {
        const val USER_ID = "user-1"
        const val NOW = 1_000L
    }
}

private class RecordingSynchronizer : Synchronizer {
    val calls = mutableListOf<String>()

    override suspend fun seed(now: Long) {
        calls += "seed:$now"
    }

    override suspend fun runPushLoop() {
        calls += "runPushLoop"
        awaitCancellation()
    }

    override suspend fun pushPendingOnce() {
        calls += "pushPendingOnce"
    }

    override suspend fun observeRealtime() {
        calls += "observeRealtime"
        awaitCancellation()
    }

    override suspend fun pullSnapshot() {
        calls += "pullSnapshot"
    }

    override suspend fun clearLocal() {
        calls += "clearLocal"
    }
}

@OptIn(SupabaseInternal::class)
private class StatusOnlyRealtime : Realtime {
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

    override suspend fun removeChannel(channel: RealtimeChannel) = error("unused")

    override fun addChannel(channel: RealtimeChannel) = error("unused")

    override suspend fun removeAllChannels() = error("unused")

    override suspend fun block() = error("unused")

    override suspend fun send(message: RealtimeMessage) = error("unused")

    override suspend fun send(message: ByteArray) = error("unused")

    override suspend fun setAuth(token: String?) = error("unused")

    override fun channel(
        channelId: String,
        builder: RealtimeChannelBuilder,
    ): RealtimeChannel = error("unused")
}
