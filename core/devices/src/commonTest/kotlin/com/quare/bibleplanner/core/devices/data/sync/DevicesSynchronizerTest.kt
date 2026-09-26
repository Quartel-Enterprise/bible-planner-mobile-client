package com.quare.bibleplanner.core.devices.data.sync

import com.quare.bibleplanner.core.devices.data.UserDevicesRemoteStore
import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import com.quare.bibleplanner.core.devices.data.local.UserDeviceLocalStore
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceDtoToEntityMapper
import com.quare.bibleplanner.core.devices.fake.FakeRealtime
import com.quare.bibleplanner.core.devices.fake.FakeUserDeviceDao
import com.quare.bibleplanner.core.devices.fake.RecordingSupabaseClient
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesSynchronizerTest {
    private val commitTimestamp = Instant.parse("2026-07-11T10:00:00Z")
    private val serializer = KotlinXSerializer(Json)
    private val firstRetryDelay = 2.seconds
    private val remoteDto = UserDeviceDto(
        id = "row-1",
        userId = USER_ID,
        deviceId = "device-1",
        name = "iPhone",
        platform = "ios",
        formFactor = "phone",
        locationCity = null,
        locationCountry = null,
        lastActiveAt = "2026-07-11T12:00:00Z",
        updatedAt = "2026-07-11T10:00:00Z",
    )
    private lateinit var synchronizer: DevicesSynchronizer
    private lateinit var dao: FakeUserDeviceDao
    private lateinit var supabase: RecordingSupabaseClient
    private lateinit var realtime: FakeRealtime
    private lateinit var userId: MutableStateFlow<String?>

    @Test
    fun `GIVEN a pending rename WHEN flushing once THEN pushes it and marks it synced`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = true,
                ),
            ),
        )

        // When
        synchronizer.pushPendingOnce()

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Patch,
            actual = request.method,
        )
        assertEquals(
            expected = "eq.row-1",
            actual = request.query["id"],
        )
        assertTrue(dao.rows.value.none { it.isNamePendingSync })
    }

    @Test
    fun `GIVEN nothing pending WHEN flushing once THEN pushes nothing`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = false,
                ),
            ),
        )

        // When
        synchronizer.pushPendingOnce()

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN no authenticated session WHEN flushing once THEN pushes nothing`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = true,
                ),
            ),
            authenticatedUserId = null,
        )

        // When
        synchronizer.pushPendingOnce()

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN a pending rename and online WHEN the push loop runs THEN pushes it and marks it synced`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = true,
                ),
            ),
        )

        // When
        runPushLoop()

        // Then
        assertEquals(
            expected = 1,
            actual = supabase.requests.size,
        )
        assertTrue(dao.rows.value.none { it.isNamePendingSync })
    }

    @Test
    fun `GIVEN a pending rename and offline WHEN the push loop runs THEN pushes nothing`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = true,
                ),
            ),
            isOnline = false,
        )

        // When
        runPushLoop()

        // Then
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN a failed push WHEN the backoff elapses THEN retries until it succeeds`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = true,
                ),
            ),
        )
        supabase.responseStatus = HttpStatusCode.InternalServerError
        runPushLoop()

        // When
        supabase.responseStatus = HttpStatusCode.OK
        advanceTimeBy(firstRetryDelay)
        runCurrent()

        // Then
        assertEquals(
            expected = 2,
            actual = supabase.requests.size,
        )
        assertTrue(dao.rows.value.none { it.isNamePendingSync })
    }

    @Test
    fun `GIVEN the session is lost after a failed push WHEN retrying THEN stops pushing`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    isPending = true,
                ),
            ),
        )
        supabase.responseStatus = HttpStatusCode.InternalServerError
        runPushLoop()

        // When
        userId.value = null
        advanceTimeBy(firstRetryDelay)
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = supabase.requests.size,
        )
    }

    @Test
    fun `GIVEN realtime changes WHEN observing THEN applies upserts and removals locally`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-2",
                    isPending = false,
                ),
            ),
            actions = flowOf(
                PostgresAction.Insert(
                    record = Json.encodeToJsonElement(remoteDto).jsonObject,
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
                PostgresAction.Delete(
                    oldRecord = JsonObject(mapOf("id" to JsonPrimitive("row-2"))),
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
            ),
        )

        // When
        synchronizer.observeRealtime()

        // Then
        assertEquals(
            expected = listOf("row-1"),
            actual = dao.rows.value.map { it.id },
        )
    }

    @Test
    fun `GIVEN a failing realtime stream WHEN observing THEN swallows the failure`() = runTest {
        // Given
        prepareScenario(actions = flow { error("socket closed") })

        // When
        synchronizer.observeRealtime()

        // Then
        assertEquals(
            expected = listOf("user_devices_$USER_ID"),
            actual = realtime.removedChannelIds,
        )
    }

    @Test
    fun `GIVEN no authenticated session WHEN observing realtime THEN opens no channel`() = runTest {
        // Given
        prepareScenario(authenticatedUserId = null)

        // When
        synchronizer.observeRealtime()

        // Then
        assertTrue(realtime.createdChannelIds.isEmpty())
    }

    @Test
    fun `GIVEN a remote snapshot WHEN pulling THEN applies it and drops the rows gone remotely`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-2",
                    isPending = false,
                ),
            ),
            responseBody = Json.encodeToString(listOf(remoteDto)),
        )

        // When
        synchronizer.pullSnapshot()

        // Then
        assertEquals(
            expected = listOf("row-1"),
            actual = dao.rows.value.map { it.id },
        )
    }

    @Test
    fun `GIVEN an empty remote snapshot WHEN pulling THEN clears every local row`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-2",
                    isPending = false,
                ),
            ),
        )

        // When
        synchronizer.pullSnapshot()

        // Then
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun `GIVEN no authenticated session WHEN pulling THEN leaves the local rows alone`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-2",
                    isPending = false,
                ),
            ),
            authenticatedUserId = null,
        )

        // When
        synchronizer.pullSnapshot()

        // Then
        assertTrue(supabase.requests.isEmpty())
        assertEquals(
            expected = 1,
            actual = dao.rows.value.size,
        )
    }

    @Test
    fun `GIVEN local rows WHEN clearing local data THEN deletes them all`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-2",
                    isPending = true,
                ),
            ),
        )

        // When
        synchronizer.seed(now = 0L)
        synchronizer.clearLocal()

        // Then
        assertTrue(dao.rows.value.isEmpty())
    }

    private fun TestScope.runPushLoop() {
        backgroundScope.launch { synchronizer.runPushLoop() }
        runCurrent()
    }

    private fun entity(
        id: String,
        isPending: Boolean,
    ): UserDeviceEntity = UserDeviceEntity(
        id = id,
        deviceId = "device-$id",
        name = "Laptop",
        platform = "desktop",
        formFactor = "computer",
        locationCity = null,
        locationCountry = null,
        lastActiveAt = 1L,
        updatedAt = 1L,
        isNamePendingSync = isPending,
    )

    private fun prepareScenario(
        initialRows: List<UserDeviceEntity> = emptyList(),
        authenticatedUserId: String? = USER_ID,
        isOnline: Boolean = true,
        responseBody: String = "[]",
        actions: Flow<PostgresAction> = flowOf(),
    ) {
        dao = FakeUserDeviceDao(initialRows = initialRows)
        supabase = RecordingSupabaseClient(responseBody = responseBody)
        realtime = FakeRealtime(actions = actions)
        userId = MutableStateFlow(authenticatedUserId)
        synchronizer = DevicesSynchronizer(
            localStore = UserDeviceLocalStore(
                userDeviceDao = dao,
                dtoToEntityMapper = UserDeviceDtoToEntityMapper(),
            ),
            remoteStore = UserDevicesRemoteStore(
                supabaseClient = supabase.client,
                realtime = realtime,
                functions = supabase.functions,
                json = Json,
            ),
            networkConnectivityObserver = { flowOf(isOnline) },
            getAuthenticatedUserId = { userId.value },
        )
    }

    private companion object {
        const val USER_ID = "user-1"
    }
}
