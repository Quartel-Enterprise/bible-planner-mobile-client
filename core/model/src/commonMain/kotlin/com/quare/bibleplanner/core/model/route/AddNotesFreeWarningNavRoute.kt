package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class AddNotesFreeWarningNavRoute(
    val maxFreeNotesAmount: Int,
)
