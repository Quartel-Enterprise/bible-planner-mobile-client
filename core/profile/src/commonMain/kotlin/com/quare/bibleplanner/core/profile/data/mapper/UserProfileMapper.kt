package com.quare.bibleplanner.core.profile.data.mapper

import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import com.quare.bibleplanner.core.user.domain.model.UserModel

internal class UserProfileMapper {
    fun map(
        user: UserModel,
        entity: ProfileEntity?,
    ): UserProfile {
        val pendingBytes = entity?.pendingAvatarBytes
        val customAvatarUrl = entity?.avatarUrl
        val providerPhoto = user.photo
        val avatar = when {
            pendingBytes != null -> AvatarSource.Pending(pendingBytes)
            !customAvatarUrl.isNullOrEmpty() -> AvatarSource.Remote(customAvatarUrl)
            customAvatarUrl == REMOVED_AVATAR_URL -> AvatarSource.None
            providerPhoto != null -> AvatarSource.Remote(providerPhoto)
            else -> AvatarSource.None
        }
        return UserProfile(
            userId = user.id,
            displayName = entity?.displayName ?: user.name,
            email = user.email,
            avatar = avatar,
            hasVisiblePhoto = avatar != AvatarSource.None,
            hasProviderPhoto = providerPhoto != null,
            isUsingProviderPhoto = providerPhoto != null &&
                pendingBytes == null &&
                customAvatarUrl == null,
            provider = user.provider,
        )
    }

    companion object {
        const val REMOVED_AVATAR_URL = ""
    }
}
