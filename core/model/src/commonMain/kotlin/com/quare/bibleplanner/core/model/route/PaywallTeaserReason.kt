package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class PaywallTeaserReason {
    HIGHLIGHT_CUSTOM_COLOR,
    ;

    val key: String
        get() = name.lowercase()
}
