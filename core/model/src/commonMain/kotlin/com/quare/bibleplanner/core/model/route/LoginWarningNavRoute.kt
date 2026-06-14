package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/**
 * Dialog telling a logged-out user they must sign in before a setting can be persisted/synced.
 * [reason] is a [com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason] name, kept as a
 * String for type-safe navigation.
 */
@Serializable
data class LoginWarningNavRoute(
    val reason: String,
)
