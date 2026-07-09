package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class DeleteVersionNavRoute(
    val versionId: String,
) : NavKey
