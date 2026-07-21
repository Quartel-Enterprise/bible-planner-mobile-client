package com.quare.bibleplanner.core.profile.domain.usecase.impl

import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import kotlinx.coroutines.flow.Flow

internal class ObserveUserProfileUseCase(
    private val profileRepository: ProfileRepository,
) : ObserveUserProfile {
    override fun invoke(): Flow<UserProfile?> = profileRepository.observeProfile()
}
