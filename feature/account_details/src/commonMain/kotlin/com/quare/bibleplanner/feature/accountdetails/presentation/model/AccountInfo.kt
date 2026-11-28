package com.quare.bibleplanner.feature.accountdetails.presentation.model

import kotlin.time.Instant

data class AccountInfo(
    val loginMethod: LoginMethod,
    val lastSignInAt: Instant?,
    val createdAt: Instant?,
)
