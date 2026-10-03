package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

// Why: reason holds a LoginWarningReason name as a String to keep the route serializable.
@Serializable
data class LoginWarningNavRoute(
    val reason: String,
) : NavRoute
