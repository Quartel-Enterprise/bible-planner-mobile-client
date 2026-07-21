package com.quare.bibleplanner.feature.editprofile.domain.usecase.impl

import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.feature.editprofile.domain.usecase.UpdateDisplayName

internal class UpdateDisplayNameUseCase(
    private val profileRepository: ProfileRepository,
) : UpdateDisplayName {
    override suspend fun invoke(displayName: String) {
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return
        profileRepository.setDisplayName(trimmed)
    }
}
