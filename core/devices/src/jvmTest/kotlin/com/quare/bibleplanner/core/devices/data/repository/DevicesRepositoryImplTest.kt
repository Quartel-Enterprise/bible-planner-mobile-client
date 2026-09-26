package com.quare.bibleplanner.core.devices.data.repository

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.devices.data.DeviceIdProvider
import com.quare.bibleplanner.core.devices.data.DeviceInfoProvider
import com.quare.bibleplanner.core.devices.data.UserDevicesRemoteStore
import com.quare.bibleplanner.core.devices.data.dto.RegisterDeviceRequest
import com.quare.bibleplanner.core.devices.data.local.UserDeviceLocalStore
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceDtoToEntityMapper
import com.quare.bibleplanner.core.devices.data.mapper.UserDeviceEntityToDomainMapper
import com.quare.bibleplanner.core.devices.fake.FakePreferencesDataStore
import com.quare.bibleplanner.core.devices.fake.FakeRealtime
import com.quare.bibleplanner.core.devices.fake.FakeUserDeviceDao
import com.quare.bibleplanner.core.devices.fake.RecordingSupabaseClient
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DevicesRepositoryImplTest {
    private val deviceInfoProvider = DeviceInfoProvider()
    private lateinit var repository: DevicesRepositoryImpl
    private lateinit var dao: FakeUserDeviceDao
    private lateinit var supabase: RecordingSupabaseClient

    @Test
    fun `GIVEN several devices WHEN observing THEN lists this device first and then the most recently active`() =
        runTest {
            // Given
            prepareScenario(
                initialRows = listOf(
                    entity(
                        id = "old",
                        deviceId = "other-1",
                        lastActiveAt = 1L,
                    ),
                    entity(
                        id = "recent",
                        deviceId = "other-2",
                        lastActiveAt = 3L,
                    ),
                    entity(
                        id = "this",
                        deviceId = CURRENT_DEVICE_ID,
                        lastActiveAt = 2L,
                    ),
                ),
            )

            // When
            val devices = repository.observeDevices().first()

            // Then
            assertEquals(
                expected = listOf("this", "recent", "old"),
                actual = devices.map { it.id },
            )
            assertEquals(
                expected = listOf(true, false, false),
                actual = devices.map { it.isCurrentDevice },
            )
        }

    @Test
    fun `GIVEN a device WHEN renaming it THEN stores the name locally as a pending change`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    deviceId = "other",
                    lastActiveAt = 1L,
                ),
            ),
        )

        // When
        repository.renameDevice(
            deviceRowId = "row-1",
            name = "Kitchen tablet",
        )

        // Then
        val row = dao.rows.value.single()
        assertEquals(
            expected = "Kitchen tablet",
            actual = row.name,
        )
        assertEquals(
            expected = NOW,
            actual = row.updatedAt,
        )
        assertTrue(row.isNamePendingSync)
        assertTrue(supabase.requests.isEmpty())
    }

    @Test
    fun `GIVEN a successful revoke WHEN signing a device out THEN removes it locally`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    deviceId = "other",
                    lastActiveAt = 1L,
                ),
            ),
        )

        // When
        val result = repository.signOutDevice("row-1")

        // Then
        assertTrue(result.isSuccess)
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun `GIVEN a failing revoke WHEN signing a device out THEN fails and keeps it locally`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    id = "row-1",
                    deviceId = "other",
                    lastActiveAt = 1L,
                ),
            ),
        )
        supabase.responseStatus = HttpStatusCode.InternalServerError

        // When
        val result = repository.signOutDevice("row-1")

        // Then
        assertTrue(result.isFailure)
        assertEquals(
            expected = 1,
            actual = dao.rows.value.size,
        )
    }

    @Test
    fun `GIVEN this device WHEN registering it THEN sends its installation id and description`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = repository.registerCurrentDevice()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(
            expected = RegisterDeviceRequest(
                deviceId = CURRENT_DEVICE_ID,
                name = deviceInfoProvider.deviceName,
                platform = "desktop",
                formFactor = "computer",
            ),
            actual = Json.decodeFromString<RegisterDeviceRequest>(
                supabase.requests
                    .single()
                    .body,
            ),
        )
    }

    @Test
    fun `GIVEN this device WHEN unregistering it THEN deletes its row by installation id`() = runTest {
        // Given
        prepareScenario()

        // When
        val result = repository.unregisterCurrentDevice()

        // Then
        assertTrue(result.isSuccess)
        val request = supabase.requests.single()
        assertEquals(
            expected = HttpMethod.Delete,
            actual = request.method,
        )
        assertEquals(
            expected = "eq.$CURRENT_DEVICE_ID",
            actual = request.query["device_id"],
        )
    }

    private fun entity(
        id: String,
        deviceId: String,
        lastActiveAt: Long,
    ): UserDeviceEntity = UserDeviceEntity(
        id = id,
        deviceId = deviceId,
        name = "Device $id",
        platform = "ios",
        formFactor = "phone",
        locationCity = null,
        locationCountry = null,
        lastActiveAt = lastActiveAt,
        updatedAt = 1L,
        isNamePendingSync = false,
    )

    private fun prepareScenario(initialRows: List<UserDeviceEntity> = emptyList()) {
        dao = FakeUserDeviceDao(initialRows = initialRows)
        supabase = RecordingSupabaseClient(responseBody = "[]")
        repository = DevicesRepositoryImpl(
            localStore = UserDeviceLocalStore(
                userDeviceDao = dao,
                dtoToEntityMapper = UserDeviceDtoToEntityMapper(),
            ),
            remoteStore = UserDevicesRemoteStore(
                supabaseClient = supabase.client,
                realtime = FakeRealtime(actions = emptyFlow()),
                functions = supabase.functions,
                json = Json,
            ),
            deviceIdProvider = DeviceIdProvider(
                FakePreferencesDataStore(
                    preferencesOf(stringPreferencesKey("device_installation_id") to CURRENT_DEVICE_ID),
                ),
            ),
            deviceInfoProvider = deviceInfoProvider,
            entityToDomainMapper = UserDeviceEntityToDomainMapper(),
            currentTimestampProvider = { NOW },
        )
    }

    private companion object {
        const val CURRENT_DEVICE_ID = "installation-1"
        const val NOW = 9_000L
    }
}
