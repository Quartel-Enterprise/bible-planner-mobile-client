package com.quare.bibleplanner.core.devices.data.mapper

import com.quare.bibleplanner.core.devices.data.dto.UserDeviceDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Instant

class UserDeviceDtoToEntityMapperTest {
    private val mapper = UserDeviceDtoToEntityMapper()

    @Test
    fun `GIVEN a dto WHEN mapping THEN parses timestamps to epoch millis and is not pending`() {
        // Given
        val dto = UserDeviceDto(
            id = "row-1",
            userId = "user-1",
            deviceId = "device-1",
            name = "iPad Air",
            platform = "ios",
            formFactor = "tablet",
            locationCity = "São Paulo",
            locationCountry = "BR",
            lastActiveAt = "2026-07-11T12:00:00Z",
            updatedAt = "2026-07-11T09:30:00Z",
        )

        // When
        val entity = mapper.map(dto)

        // Then
        assertEquals("row-1", entity.id)
        assertEquals(Instant.parse("2026-07-11T12:00:00Z").toEpochMilliseconds(), entity.lastActiveAt)
        assertEquals(Instant.parse("2026-07-11T09:30:00Z").toEpochMilliseconds(), entity.updatedAt)
        assertFalse(entity.isNamePendingSync)
    }
}
