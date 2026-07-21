package com.quare.bibleplanner.core.profile.domain.model

data class UserProfile(
    val userId: String,
    val displayName: String?,
    val email: String,
    val avatar: AvatarSource,
    val hasVisiblePhoto: Boolean,
    val hasProviderPhoto: Boolean,
    val isUsingProviderPhoto: Boolean,
    val provider: String?,
)
