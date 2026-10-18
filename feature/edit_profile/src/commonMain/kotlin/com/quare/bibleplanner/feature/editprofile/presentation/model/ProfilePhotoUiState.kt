package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import software.quare.loadable.Loadable

internal data class ProfilePhotoUiState(
    val profile: Loadable<UserProfile?>,
    val isCameraAvailable: Boolean,
)
