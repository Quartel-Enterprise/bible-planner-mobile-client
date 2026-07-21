package com.quare.bibleplanner.core.profile.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.profile.data.mapper.UserProfileMapper
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.repository.ProfileRepository
import com.quare.bibleplanner.core.provider.room.dao.ProfileDao
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
    private val observeCurrentUser: ObserveCurrentUser,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val userProfileMapper: UserProfileMapper,
) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                profileDao
                    .observeProfile(user.id)
                    .map { entity ->
                        userProfileMapper.map(
                            user = user,
                            entity = entity,
                        )
                    }
            }
        }.distinctUntilChanged()

    override suspend fun setDisplayName(displayName: String) {
        val userId = getAuthenticatedUserId() ?: return
        profileDao.setDisplayNameLocal(
            id = userId,
            displayName = displayName,
            updatedAt = currentTimestampProvider.getCurrentTimestamp(),
        )
    }

    override suspend fun setPhoto(bytes: ByteArray) {
        setAvatar(
            avatarUrl = null,
            pendingAvatarBytes = bytes,
        )
    }

    override suspend fun removePhoto() {
        setAvatar(
            avatarUrl = UserProfileMapper.REMOVED_AVATAR_URL,
            pendingAvatarBytes = null,
        )
    }

    override suspend fun useProviderPhoto() {
        setAvatar(
            avatarUrl = null,
            pendingAvatarBytes = null,
        )
    }

    private suspend fun setAvatar(
        avatarUrl: String?,
        pendingAvatarBytes: ByteArray?,
    ) {
        val userId = getAuthenticatedUserId() ?: return
        profileDao.setAvatarLocal(
            id = userId,
            avatarUrl = avatarUrl,
            pendingAvatarBytes = pendingAvatarBytes,
            updatedAt = currentTimestampProvider.getCurrentTimestamp(),
        )
    }
}
