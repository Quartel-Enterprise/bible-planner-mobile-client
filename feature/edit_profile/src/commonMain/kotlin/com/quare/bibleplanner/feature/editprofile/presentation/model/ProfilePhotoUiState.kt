package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.profile.domain.model.UserProfile

internal data class ProfilePhotoUiState(
    val profile: Loadable<UserProfile?>,
    val isCameraAvailable: Boolean,
)
