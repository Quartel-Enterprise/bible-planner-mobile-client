package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class PaywallTeaserReason {
    HIGHLIGHT_CUSTOM_COLOR,
    CHAPTER_STUDY_LIMIT,
    ;

    val key: String
        get() = name.lowercase()
}
