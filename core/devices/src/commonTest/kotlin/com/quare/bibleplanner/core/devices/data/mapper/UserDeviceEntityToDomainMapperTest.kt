package com.quare.bibleplanner.core.devices.data.mapper

import com.quare.bibleplanner.core.devices.domain.model.DeviceFormFactor
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserDeviceEntityToDomainMapperTest {
    private val mapper = UserDeviceEntityToDomainMapper()

    @Test
    fun `GIVEN an entity matching the current device id WHEN mapping THEN marks it as current`() {
        val model = mapper.map(entity(deviceId = "device-1"), currentDeviceId = "device-1")

        assertTrue(model.isCurrentDevice)
        assertEquals(DeviceFormFactor.TABLET, model.formFactor)
    }

    @Test
    fun `GIVEN an entity not matching the current device id WHEN mapping THEN is not current`() {
        val model = mapper.map(entity(deviceId = "device-2"), currentDeviceId = "device-1")

        assertFalse(model.isCurrentDevice)
    }

    private fun entity(deviceId: String) = UserDeviceEntity(
        id = "row-1",
        deviceId = deviceId,
        name = "iPad Air",
        platform = "ios",
        formFactor = "tablet",
        locationCity = "São Paulo",
        locationCountry = "BR",
        lastActiveAt = 1_000L,
        updatedAt = 1_000L,
        isNamePendingSync = false,
    )
}
