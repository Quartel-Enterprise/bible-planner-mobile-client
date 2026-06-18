package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/**
 * Dialog nudging a logged-out, online user to sign in so the change they just made locally gets
 * backed up and synced. A single global dialog, hence no parameters.
 */
@Serializable
data object LoginSyncNudgeNavRoute
