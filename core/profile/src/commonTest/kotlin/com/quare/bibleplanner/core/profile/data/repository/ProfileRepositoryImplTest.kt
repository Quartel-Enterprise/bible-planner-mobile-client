package com.quare.bibleplanner.core.profile.data.repository

import com.quare.bibleplanner.core.profile.data.mapper.UserProfileMapper
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.fake.FakeProfileDao
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import com.quare.bibleplanner.core.user.domain.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileRepositoryImplTest {
    private val user = UserModel(
        id = USER_ID,
        name = "Provider Name",
        email = "user@example.com",
        photo = PROVIDER_PHOTO,
        provider = "google",
        lastSignInAt = null,
        createdAt = null,
    )
    private lateinit var repository: ProfileRepositoryImpl
    private lateinit var dao: FakeProfileDao

    @Test
    fun `GIVEN nobody signed in WHEN observing the profile THEN returns no profile`() = runTest {
        // Given
        prepareScenario(currentUser = null)

        // When
        val profile = repository.observeProfile().first()

        // Then
        assertNull(profile)
    }

    @Test
    fun `GIVEN a signed in user with a local profile row WHEN observing the profile THEN combines them`() = runTest {
        // Given
        prepareScenario(
            initialRows = listOf(
                entity(
                    displayName = "Custom Name",
                    avatarUrl = CUSTOM_PHOTO,
                ),
            ),
        )

        // When
        val profile = repository.observeProfile().first()

        // Then
        assertEquals(
            expected = "Custom Name",
            actual = profile?.displayName,
        )
        assertEquals(
            expected = AvatarSource.Remote(CUSTOM_PHOTO),
            actual = profile?.avatar,
        )
    }

    @Test
    fun `GIVEN no local profile WHEN setting the display name THEN stores it as a pending change`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setDisplayName("New Name")

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    displayName = "New Name",
                    updatedAt = NOW,
                    displayNamePendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `GIVEN an existing profile with a photo WHEN setting the display name THEN keeps its photo`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(entity(avatarUrl = CUSTOM_PHOTO)))

        // When
        repository.setDisplayName("New Name")

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    displayName = "New Name",
                    avatarUrl = CUSTOM_PHOTO,
                    updatedAt = NOW,
                    displayNamePendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `GIVEN a picked photo WHEN setting it THEN keeps it as pending bytes until it is uploaded`() = runTest {
        // Given
        prepareScenario()
        val bytes = byteArrayOf(1, 2, 3)

        // When
        repository.setPhoto(bytes)

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    pendingAvatarBytes = bytes,
                    updatedAt = NOW,
                    avatarPendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `GIVEN a custom photo WHEN removing it THEN marks it with the removed avatar url`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(entity(avatarUrl = CUSTOM_PHOTO)))

        // When
        repository.removePhoto()

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    avatarUrl = UserProfileMapper.REMOVED_AVATAR_URL,
                    updatedAt = NOW,
                    avatarPendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `GIVEN a custom photo WHEN using the provider photo THEN clears the custom avatar`() = runTest {
        // Given
        prepareScenario(initialRows = listOf(entity(avatarUrl = CUSTOM_PHOTO)))

        // When
        repository.useProviderPhoto()

        // Then
        assertEquals(
            expected = listOf(
                entity(
                    avatarUrl = null,
                    updatedAt = NOW,
                    avatarPendingSync = true,
                ),
            ),
            actual = dao.rows.value,
        )
    }

    @Test
    fun `GIVEN nobody signed in WHEN editing the profile THEN ignores the edits`() = runTest {
        // Given
        prepareScenario(currentUser = null)

        // When
        repository.setDisplayName("New Name")
        repository.setPhoto(byteArrayOf(1))

        // Then
        assertTrue(dao.rows.value.isEmpty())
    }

    private fun entity(
        displayName: String? = null,
        avatarUrl: String? = null,
        pendingAvatarBytes: ByteArray? = null,
        updatedAt: Long = OLD_TIMESTAMP,
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

    private fun prepareScenario(
        currentUser: UserModel? = user,
        initialRows: List<ProfileEntity> = emptyList(),
    ) {
        dao = FakeProfileDao(initialRows = initialRows)
        repository = ProfileRepositoryImpl(
            profileDao = dao,
            observeCurrentUser = { MutableStateFlow(currentUser) },
            getAuthenticatedUserId = { currentUser?.id },
            currentTimestampProvider = { NOW },
            userProfileMapper = UserProfileMapper(),
        )
    }

    private companion object {
        const val USER_ID = "user-id"
        const val OLD_TIMESTAMP = 100L
        const val NOW = 5_000L
        const val PROVIDER_PHOTO = "https://provider.example/photo.jpg"
        const val CUSTOM_PHOTO = "https://storage.example/avatar.jpg"
    }
}
