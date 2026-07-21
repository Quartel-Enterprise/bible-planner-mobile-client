package com.quare.bibleplanner.core.profile.domain.repository

import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>

    suspend fun setDisplayName(displayName: String)

    suspend fun setPhoto(bytes: ByteArray)

    suspend fun removePhoto()

    suspend fun useProviderPhoto()
}
