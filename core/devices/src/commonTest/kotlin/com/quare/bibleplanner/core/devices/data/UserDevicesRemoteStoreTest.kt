package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.data.dto.RegisterDeviceRequest
import com.quare.bibleplanner.core.devices.data.dto.RevokeDeviceRequest
import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import com.quare.bibleplanner.core.devices.data.model.DeviceChange
import com.quare.bibleplanner.core.devices.fake.FakeRealtime
import com.quare.bibleplanner.core.devices.fake.RecordingSupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class UserDevicesRemoteStoreTest {
    private val commitTimestamp = Instant.parse("2026-07-11T10:00:00Z")
    private val serializer = KotlinXSerializer(Json)
    private val dto = UserDeviceDto(
        id = "row-1",
        userId = USER_ID,
        deviceId = "device-1",
        name = "iPhone",
        platform = "ios",
        formFactor = "phone",
        locationCity = "Recife",
        locationCountry = "BR",
        lastActiveAt = "2026-07-11T12:00:00Z",
        updatedAt = "2026-07-11T10:00:00Z",
    )
    private lateinit var remoteStore: UserDevicesRemoteStore
    private lateinit var supabase: RecordingSupabaseClient
    private lateinit var realtime: FakeRealtime

    @Test
    fun `GIVEN remote rows WHEN fetching THEN returns the rows of the user`() = runTest {
        // Given
        prepareScenario(responseBody = Json.encodeToString(listOf(dto)))

        // When
        val rows = remoteStore.fetch(USER_ID)

        // Then
        assertEquals(
            expected = listOf(dto),
            actual = rows,
        )
        assertEquals(
            expected = "eq.$USER_ID",
            actual = supabase.requests
                .single()
                .query["user_id"],
        )
    }

    @Test
    fun `GIVEN a new name WHEN renaming THEN patches the name and stamp of that row`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.rename(
            deviceRowId = "row-1",
            name = "Work laptop",
            updatedAt = "2026-07-11T10:00:00Z",
        )

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
        assertEquals(
            expected = JsonObject(
                mapOf(
                    "name" to JsonPrimitive("Work laptop"),
                    "updated_at" to JsonPrimitive("2026-07-11T10:00:00Z"),
                ),
            ),
            actual = Json.parseToJsonElement(request.body),
        )
    }

    @Test
    fun `GIVEN a device row WHEN signing it out THEN calls the revoke function with the row id`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.signOutDevice("row-1")

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = "/functions/v1/revoke-device-session",
            actual = request.path,
        )
        assertEquals(
            expected = RevokeDeviceRequest("row-1"),
            actual = Json.decodeFromString<RevokeDeviceRequest>(request.body),
        )
    }

    @Test
    fun `GIVEN this device WHEN deleting it THEN deletes the rows with its installation id`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.deleteOwnDevice("device-1")

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Delete,
            actual = request.method,
        )
        assertEquals(
            expected = "eq.device-1",
            actual = request.query["device_id"],
        )
    }

    @Test
    fun `GIVEN a registration request WHEN registering THEN calls the register function with it`() = runTest {
        // Given
        prepareScenario()
        val registration = RegisterDeviceRequest(
            deviceId = "device-1",
            name = "iPhone",
            platform = "ios",
            formFactor = "phone",
        )

        // When
        remoteStore.registerCurrentDevice(registration)

        // Then
        val request = supabase.requests.single()
        assertEquals(
            expected = "/functions/v1/register-device",
            actual = request.path,
        )
        assertEquals(
            expected = registration,
            actual = Json.decodeFromString<RegisterDeviceRequest>(request.body),
        )
    }

    @Test
    fun `GIVEN realtime actions WHEN observing THEN maps upserts and deletions and skips the rest`() = runTest {
        // Given
        val record = Json.encodeToJsonElement(dto).jsonObject
        prepareScenario(
            actions = listOf(
                PostgresAction.Insert(
                    record = record,
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
                PostgresAction.Update(
                    record = record,
                    oldRecord = record,
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
                PostgresAction.Delete(
                    oldRecord = JsonObject(emptyMap()),
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
                PostgresAction.Select(
                    record = record,
                    columns = emptyList(),
                    commitTimestamp = commitTimestamp,
                    serializer = serializer,
                ),
            ),
        )

        // When
        val changes = remoteStore.observeChanges(USER_ID).toList()

        // Then
        assertEquals(
            expected = listOf(
                DeviceChange.Upserted(dto),
                DeviceChange.Upserted(dto),
                DeviceChange.Removed("row-2"),
            ),
            actual = changes,
        )
    }

    @Test
    fun `GIVEN an observed stream WHEN it ends THEN removes the channel of the user`() = runTest {
        // Given
        prepareScenario()

        // When
        remoteStore.observeChanges(USER_ID).toList()

        // Then
        assertEquals(
            expected = listOf("user_devices_$USER_ID"),
            actual = realtime.subscribedChannelIds,
        )
        assertEquals(
            expected = listOf("user_devices_$USER_ID"),
            actual = realtime.removedChannelIds,
        )
    }

    private fun prepareScenario(
        responseBody: String = "[]",
        actions: List<PostgresAction> = emptyList(),
    ) {
        supabase = RecordingSupabaseClient(responseBody = responseBody)
        realtime = FakeRealtime(actions = flowOf(*actions.toTypedArray()))
        remoteStore = UserDevicesRemoteStore(
            supabaseClient = supabase.client,
            realtime = realtime,
            functions = supabase.functions,
            json = Json,
        )
    }

    private companion object {
        const val USER_ID = "user-1"
    }
}
