package com.quare.bibleplanner.core.profile.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileMapperTest {
    private lateinit var mapper: ProfileMapper

    @BeforeTest
    fun setUp() {
        mapper = ProfileMapper()
    }

    @Test
    fun `GIVEN only the name changed WHEN mapping to a dto THEN marks only the display name dirty`() {
        // When
        val dto = mapper.toDto(
            userId = USER_ID,
            entity = entity(
                displayName = "New Name",
                displayNamePendingSync = true,
            ),
        )

        // Then
        assertTrue(dto.isDisplayNameDirty)
        assertFalse(dto.isAvatarDirty)
    }

    @Test
    fun `GIVEN only the photo changed WHEN mapping to a dto THEN marks only the avatar dirty`() {
        // When
        val dto = mapper.toDto(
            userId = USER_ID,
            entity = entity(
                avatarUrl = "https://storage.example/avatar.jpg",
                avatarPendingSync = true,
            ),
        )

        // Then
        assertFalse(dto.isDisplayNameDirty)
        assertTrue(dto.isAvatarDirty)
    }

    @Test
    fun `GIVEN avatar bytes waiting for upload WHEN mapping to a dto THEN holds the avatar back`() {
        // When
        val dto = mapper.toDto(
            userId = USER_ID,
            entity = entity(
                pendingAvatarBytes = byteArrayOf(1, 2, 3),
                avatarPendingSync = true,
            ),
        )

        // Then
        assertFalse(dto.isAvatarDirty)
    }

    @Test
    fun `GIVEN an epoch timestamp WHEN mapping to a dto THEN converts it to an ISO-8601 instant`() {
        // When
        val dto = mapper.toDto(
            userId = USER_ID,
            entity = entity(updatedAt = 0L),
        )

        // Then
        assertEquals("1970-01-01T00:00:00Z", dto.updatedAt)
        assertEquals(0L, mapper.toEpochMillis(dto.updatedAt))
    }

    private fun entity(
        displayName: String? = null,
        avatarUrl: String? = null,
        pendingAvatarBytes: ByteArray? = null,
        updatedAt: Long = 1L,
        displayNamePendingSync: Boolean = false,
        avatarPendingSync: Boolean = false,
    ): ProfileEntity = ProfileEntity(
        id = USER_ID,
        displayName = displayName,
        avatarUrl = avatarUrl,
        pendingAvatarBytes = pendingAvatarBytes,
        updatedAt = updatedAt,
        displayNamePendingSync = displayNamePendingSync,
        avatarPendingSync = avatarPendingSync,
    )

    private companion object {
        const val USER_ID = "user-id"
    }
}
