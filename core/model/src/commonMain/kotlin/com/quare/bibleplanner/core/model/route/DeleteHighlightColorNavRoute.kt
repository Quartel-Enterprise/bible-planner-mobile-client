package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class DeleteHighlightColorNavRoute(
    val colorKey: String,
) : NavRoute
