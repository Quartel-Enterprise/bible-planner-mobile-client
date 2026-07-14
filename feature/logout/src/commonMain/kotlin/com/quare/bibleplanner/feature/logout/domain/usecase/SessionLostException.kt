package com.quare.bibleplanner.feature.logout.domain.usecase

internal class SessionLostException(
    source: String,
    isAccessTokenExpired: Boolean,
) : Exception(
        "Session was cleared locally without an app-initiated logout " +
            "(source=$source, isAccessTokenExpired=$isAccessTokenExpired)",
    )
