package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.feature.editprofile.domain.usecase.SetProfilePhoto

internal class SetProfilePhotoUseCase(
    private val profileRepository: ProfileRepository,
) : SetProfilePhoto {
    override suspend fun invoke(bytes: ByteArray) {
        profileRepository.setPhoto(bytes)
    }
}
