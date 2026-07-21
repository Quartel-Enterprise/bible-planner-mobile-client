package com.quare.bibleplanner.core.profile.data.mapper

import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import com.quare.bibleplanner.core.user.domain.model.UserModel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserProfileMapperTest {
    private lateinit var mapper: UserProfileMapper

    @BeforeTest
    fun setUp() {
        mapper = UserProfileMapper()
    }

    @Test
    fun `falls back to the provider photo when there is no local row`() {
        // When
        val profile = mapper.map(
            user = user(photo = PROVIDER_PHOTO),
            entity = null,
        )

        // Then
        assertEquals(AvatarSource.Remote(PROVIDER_PHOTO), profile.avatar)
        assertTrue(profile.hasVisiblePhoto)
        assertTrue(profile.hasProviderPhoto)
    }

    @Test
    fun `falls back to the provider photo when the custom url is null`() {
        // When
        val profile = mapper.map(
            user = user(photo = PROVIDER_PHOTO),
            entity = entity(avatarUrl = null),
        )

        // Then
        assertEquals(AvatarSource.Remote(PROVIDER_PHOTO), profile.avatar)
    }

    @Test
    fun `keeps the photo removed instead of falling back to the provider`() {
        // When
        val profile = mapper.map(
            user = user(photo = PROVIDER_PHOTO),
            entity = entity(avatarUrl = ""),
        )

        // Then
        assertEquals(AvatarSource.None, profile.avatar)
        assertFalse(profile.hasVisiblePhoto)
    }

    @Test
    fun `prefers the custom photo over the provider one`() {
        // When
        val profile = mapper.map(
            user = user(photo = PROVIDER_PHOTO),
            entity = entity(avatarUrl = CUSTOM_PHOTO),
        )

        // Then
        assertEquals(AvatarSource.Remote(CUSTOM_PHOTO), profile.avatar)
        assertTrue(profile.hasVisiblePhoto)
    }

    @Test
    fun `prefers the not yet uploaded photo over everything else`() {
        // Given
        val bytes = byteArrayOf(1, 2, 3)

        // When
        val profile = mapper.map(
            user = user(photo = PROVIDER_PHOTO),
            entity = entity(
                avatarUrl = CUSTOM_PHOTO,
                pendingAvatarBytes = bytes,
            ),
        )

        // Then
        assertEquals(AvatarSource.Pending(bytes), profile.avatar)
        assertTrue(profile.hasVisiblePhoto)
    }

    @Test
    fun `has no avatar when neither the provider nor the user supplied one`() {
        // When
        val profile = mapper.map(
            user = user(photo = null),
            entity = null,
        )

        // Then
        assertEquals(AvatarSource.None, profile.avatar)
        assertFalse(profile.hasProviderPhoto)
    }

    @Test
    fun `prefers the custom display name over the provider one`() {
        // When
        val profile = mapper.map(
            user = user(name = "Provider Name"),
            entity = entity(displayName = "Custom Name"),
        )

        // Then
        assertEquals("Custom Name", profile.displayName)
    }

    @Test
    fun `falls back to the provider name when there is no custom one`() {
        // When
        val profile = mapper.map(
            user = user(name = "Provider Name"),
            entity = entity(displayName = null),
        )

        // Then
        assertEquals("Provider Name", profile.displayName)
    }

    @Test
    fun `has no display name when neither the provider nor the user supplied one`() {
        // When
        val profile = mapper.map(
            user = user(name = null),
            entity = null,
        )

        // Then
        assertEquals(null, profile.displayName)
    }

    private fun user(
        name: String? = "Provider Name",
        photo: String? = null,
    ) = UserModel(
        id = USER_ID,
        name = name,
        email = "user@example.com",
        photo = photo,
        provider = "google",
        lastSignInAt = null,
        createdAt = null,
    )

    private fun entity(
        displayName: String? = null,
        avatarUrl: String? = null,
        pendingAvatarBytes: ByteArray? = null,
    ) = ProfileEntity(
        id = USER_ID,
        displayName = displayName,
        avatarUrl = avatarUrl,
        pendingAvatarBytes = pendingAvatarBytes,
        updatedAt = 1L,
        displayNamePendingSync = false,
        avatarPendingSync = false,
    )

    private companion object {
        const val USER_ID = "user-id"
        const val PROVIDER_PHOTO = "https://provider.example/photo.jpg"
        const val CUSTOM_PHOTO = "https://storage.example/avatar.jpg"
    }
}
