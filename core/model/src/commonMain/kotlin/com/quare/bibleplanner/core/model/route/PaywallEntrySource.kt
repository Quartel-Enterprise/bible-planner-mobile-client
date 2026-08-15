package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class PaywallEntrySource {
    PROFILE_MENU,
    DAY_STUDY,
    DAY_STUDY_DETAIL,
    NOTES_LIMIT,
    CHAT,
    ;

    val key: String
        get() = name.lowercase()
}
