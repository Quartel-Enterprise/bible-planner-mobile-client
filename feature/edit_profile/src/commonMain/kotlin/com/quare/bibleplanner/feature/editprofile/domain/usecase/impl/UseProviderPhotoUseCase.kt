package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.feature.editprofile.domain.usecase.UseProviderPhoto

internal class UseProviderPhotoUseCase(
    private val profileRepository: ProfileRepository,
) : UseProviderPhoto {
    override suspend fun invoke() {
        profileRepository.useProviderPhoto()
    }
}
