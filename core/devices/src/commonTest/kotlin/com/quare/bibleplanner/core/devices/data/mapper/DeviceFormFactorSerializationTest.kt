package com.quare.bibleplanner.core.devices.data.mapper

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceFormFactorSerializationTest {
    @Test
    fun `GIVEN every form factor WHEN serializing and parsing back THEN gets the same form factor`() {
        // Given
        val formFactors = DeviceFormFactor.entries

        // When
        val roundTripped = formFactors.map { it.toRaw().toDeviceFormFactor() }

        // Then
        assertEquals(
            expected = formFactors,
            actual = roundTripped,
        )
    }

    @Test
    fun `GIVEN every form factor WHEN serializing THEN uses the backend raw values`() {
        // When
        val raw = DeviceFormFactor.entries.map { it.toRaw() }

        // Then
        assertEquals(
            expected = listOf("phone", "tablet", "computer", "unknown"),
            actual = raw,
        )
    }

    @Test
    fun `GIVEN an unrecognised raw value WHEN parsing THEN falls back to unknown`() {
        // When
        val formFactor = "watch".toDeviceFormFactor()

        // Then
        assertEquals(
            expected = DeviceFormFactor.UNKNOWN,
            actual = formFactor,
        )
    }
}
