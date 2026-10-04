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
    fun `GIVEN no local row WHEN mapping THEN falls back to the provider photo`() {
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
    fun `GIVEN a null custom url WHEN mapping THEN falls back to the provider photo`() {
        // When
        val profile = mapper.map(
            user = user(photo = PROVIDER_PHOTO),
            entity = entity(avatarUrl = null),
        )

        // Then
        assertEquals(AvatarSource.Remote(PROVIDER_PHOTO), profile.avatar)
    }

    @Test
    fun `GIVEN a removed photo WHEN mapping THEN keeps it removed instead of falling back to the provider`() {
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
    fun `GIVEN a custom and a provider photo WHEN mapping THEN prefers the custom photo`() {
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
    fun `GIVEN a not yet uploaded photo WHEN mapping THEN prefers it over everything else`() {
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
    fun `GIVEN no provider nor user photo WHEN mapping THEN has no avatar`() {
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
    fun `GIVEN a custom and a provider name WHEN mapping THEN prefers the custom display name`() {
        // When
        val profile = mapper.map(
            user = user(name = "Provider Name"),
            entity = entity(displayName = "Custom Name"),
        )

        // Then
        assertEquals("Custom Name", profile.displayName)
    }

    @Test
    fun `GIVEN no custom name WHEN mapping THEN falls back to the provider name`() {
        // When
        val profile = mapper.map(
            user = user(name = "Provider Name"),
            entity = entity(displayName = null),
        )

        // Then
        assertEquals("Provider Name", profile.displayName)
    }

    @Test
    fun `GIVEN no provider nor user name WHEN mapping THEN has no display name`() {
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
    ): UserModel = UserModel(
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
    ): ProfileEntity = ProfileEntity(
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
