package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class InAppUpdateNavRoute(
    val versionName: String?,
) : NavRoute
