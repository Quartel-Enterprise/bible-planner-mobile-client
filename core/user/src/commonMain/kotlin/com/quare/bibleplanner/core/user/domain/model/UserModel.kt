package com.quare.bibleplanner.core.user.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val photo: String?,
    val provider: String?,
    val lastSignInAt: Instant?,
    val createdAt: Instant?,
)
