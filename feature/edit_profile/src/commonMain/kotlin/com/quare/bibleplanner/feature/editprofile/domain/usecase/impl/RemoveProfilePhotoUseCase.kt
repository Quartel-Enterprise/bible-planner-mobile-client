package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.feature.editprofile.domain.usecase.RemoveProfilePhoto

internal class RemoveProfilePhotoUseCase(
    private val profileRepository: ProfileRepository,
) : RemoveProfilePhoto {
    override suspend fun invoke() {
        profileRepository.removePhoto()
    }
}
