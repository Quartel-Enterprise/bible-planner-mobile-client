package com.quare.bibleplanner.core.profile.data.dto

internal data class ProfileDto(
    val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val updatedAt: String,
    val isDisplayNameDirty: Boolean,
    val isAvatarDirty: Boolean,
)
