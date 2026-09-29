package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class AddNotesFreeWarningType {
    DAY,
    VERSE,
    ;

    val key: String
        get() = name.lowercase()
}
