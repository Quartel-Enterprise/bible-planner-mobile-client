package com.quare.bibleplanner.feature.editprofile.fake

import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

internal class FakeProfileRepository : ProfileRepository {
    val displayNames = mutableListOf<String>()
    val photos = mutableListOf<ByteArray>()
    var removePhotoCalls = 0
        private set
    var useProviderPhotoCalls = 0
        private set

    override fun observeProfile(): Flow<UserProfile?> = error("unused")

    override suspend fun setDisplayName(displayName: String) {
        displayNames += displayName
    }

    override suspend fun setPhoto(bytes: ByteArray) {
        photos += bytes
    }

    override suspend fun removePhoto() {
        removePhotoCalls++
    }

    override suspend fun useProviderPhoto() {
        useProviderPhotoCalls++
    }
}
