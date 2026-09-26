package com.quare.bibleplanner.core.devices.data

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quare.bibleplanner.core.devices.fake.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceIdProviderTest {
    private lateinit var provider: DeviceIdProvider

    @Test
    fun `GIVEN a stored installation id WHEN reading it THEN returns the stored id`() = runTest {
        // Given
        prepareScenario(storedId = "installation-1")

        // When
        val deviceId = provider.getOrCreate()

        // Then
        assertEquals(
            expected = "installation-1",
            actual = deviceId,
        )
    }

    @Test
    fun `GIVEN no stored installation id WHEN reading it twice THEN creates one id and keeps it`() = runTest {
        // Given
        prepareScenario(storedId = null)

        // When
        val first = provider.getOrCreate()
        val second = provider.getOrCreate()

        // Then
        assertTrue(first.isNotBlank())
        assertEquals(
            expected = first,
            actual = second,
        )
    }

    private fun prepareScenario(storedId: String?) {
        val dataStore = if (storedId == null) {
            FakePreferencesDataStore()
        } else {
            FakePreferencesDataStore(preferencesOf(stringPreferencesKey("device_installation_id") to storedId))
        }
        provider = DeviceIdProvider(dataStore)
    }
}
