package com.quare.bibleplanner.core.profile.domain.usecase.impl

import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveUserProfileUseCaseTest {
    private val profile = UserProfile(
        userId = "user-id",
        displayName = "Name",
        email = "user@example.com",
        avatar = AvatarSource.None,
        hasVisiblePhoto = false,
        hasProviderPhoto = false,
        isUsingProviderPhoto = false,
        provider = "apple",
    )

    @Test
    fun `emits the profile of the repository`() = runTest {
        // Given
        val useCase = ObserveUserProfileUseCase(FixedProfileRepository(profile))

        // When
        val observed = useCase().first()

        // Then
        assertEquals(
            expected = profile,
            actual = observed,
        )
    }
}

private class FixedProfileRepository(
    private val profile: UserProfile,
) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = flowOf(profile)

    override suspend fun setDisplayName(displayName: String) = error("unused")

    override suspend fun setPhoto(bytes: ByteArray) = error("unused")

    override suspend fun removePhoto() = error("unused")

    override suspend fun useProviderPhoto() = error("unused")
}
