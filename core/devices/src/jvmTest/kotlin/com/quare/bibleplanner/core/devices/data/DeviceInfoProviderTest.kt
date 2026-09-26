package com.quare.bibleplanner.core.devices.data

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceInfoProviderTest {
    private lateinit var provider: DeviceInfoProvider

    @BeforeTest
    fun setUp() {
        provider = DeviceInfoProvider()
    }

    @Test
    fun `GIVEN the desktop app WHEN describing the device THEN reports a desktop computer`() {
        // When
        val platform = provider.platform
        val formFactor = provider.formFactor

        // Then
        assertEquals(
            expected = "desktop",
            actual = platform,
        )
        assertEquals(
            expected = DeviceFormFactor.COMPUTER,
            actual = formFactor,
        )
    }

    @Test
    fun `GIVEN the desktop app WHEN naming the device THEN never returns a blank name`() {
        // When
        val name = provider.deviceName

        // Then
        assertTrue(name.isNotBlank())
    }
}
