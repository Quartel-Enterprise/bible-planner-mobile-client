package com.quare.bibleplanner.core.user.data.mapper

import com.quare.bibleplanner.core.user.domain.model.UserModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class SessionUserMapper {
    fun map(sessionUser: UserInfo): UserModel? = sessionUser.userMetadata?.let { metadata ->
        UserModel(
            // The available metadata keys vary per provider — e.g. Google provides
            // avatar_url while Apple does not, and Apple uses full_name alongside name.
            photo = metadata.stringOrNull(KEY_AVATAR_URL),
            name = metadata.stringOrNull(KEY_NAME)
                ?: metadata.stringOrNull(KEY_FULL_NAME)
                ?: return@let null,
            id = sessionUser.id,
            email = sessionUser.email ?: return@let null,
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull

    private companion object {
        const val KEY_AVATAR_URL = "avatar_url"
        const val KEY_NAME = "name"
        const val KEY_FULL_NAME = "full_name"
    }
}
