package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class PaywallTeaserReason {
    HIGHLIGHT_CUSTOM_COLOR,
    CHAPTER_STUDY_LIMIT,
    CHAPTER_LISTENING_LIMIT,
    CHAPTER_LISTENING_FREE_LIMIT,
    CHAPTER_LISTENING_PRO_ONLY,
    ;

    val key: String
        get() = name.lowercase()
}
