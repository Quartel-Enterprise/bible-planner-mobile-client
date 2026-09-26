package com.quare.bibleplanner.core.provider.room.dao

import com.quare.bibleplanner.core.provider.room.createInMemoryDatabase
import com.quare.bibleplanner.core.provider.room.db.AppDatabase
import com.quare.bibleplanner.core.provider.room.entity.UserDeviceEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UserDeviceDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: UserDeviceDao

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        dao = database.userDeviceDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun `GIVEN a pending rename WHEN applying an older remote name THEN keeps the local name`() = runTest {
        // Given
        dao.upsert(device(name = "iPhone"))
        dao.renameLocal(
            id = ROW_ID,
            name = "My phone",
            now = 50L,
        )

        // When
        dao.applyRemoteName(
            id = ROW_ID,
            name = "Remote name",
            remoteUpdatedAt = 60L,
        )

        // Then
        assertEquals(
            expected = device(name = "My phone").copy(
                updatedAt = 50L,
                isNamePendingSync = true,
            ),
            actual = dao.getById(ROW_ID),
        )
    }

    @Test
    fun `GIVEN a synced device WHEN applying a newer remote name THEN takes the remote name`() = runTest {
        // Given
        dao.upsert(device(name = "iPhone"))

        // When
        dao.applyRemoteName(
            id = ROW_ID,
            name = "Remote name",
            remoteUpdatedAt = 60L,
        )

        // Then
        assertEquals(
            expected = "Remote name",
            actual = dao.getById(ROW_ID)?.name,
        )
    }

    private fun device(name: String): UserDeviceEntity = UserDeviceEntity(
        id = ROW_ID,
        deviceId = "device-1",
        name = name,
        platform = "ios",
        formFactor = "phone",
        locationCity = "Recife",
        locationCountry = "BR",
        lastActiveAt = 1L,
        updatedAt = 1L,
        isNamePendingSync = false,
    )

    private companion object {
        const val ROW_ID = "row-1"
    }
}
