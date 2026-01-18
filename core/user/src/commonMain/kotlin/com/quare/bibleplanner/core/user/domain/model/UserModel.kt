package com.quare.bibleplanner.core.user.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val photo: String?,
)
