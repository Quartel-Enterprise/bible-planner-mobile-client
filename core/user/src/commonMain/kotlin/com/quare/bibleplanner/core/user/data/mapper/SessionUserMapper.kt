package com.quare.bibleplanner.core.user.data.mapper

import com.quare.bibleplanner.core.user.domain.model.UserModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class SessionUserMapper {
    fun map(sessionUser: UserInfo): UserModel? = sessionUser.userMetadata?.let { metadata ->
        UserModel(
            photo = metadata.stringOrNull(KEY_AVATAR_URL),
            name = metadata.stringOrNull(KEY_NAME)
                ?: metadata.stringOrNull(KEY_FULL_NAME),
            id = sessionUser.id,
            email = sessionUser.email ?: return@let null,
            provider = sessionUser.appMetadata?.stringOrNull(KEY_PROVIDER),
            lastSignInAt = sessionUser.lastSignInAt,
            createdAt = sessionUser.createdAt,
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull

    private companion object {
        const val KEY_AVATAR_URL = "avatar_url"
        const val KEY_NAME = "name"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_PROVIDER = "provider"
    }
}
