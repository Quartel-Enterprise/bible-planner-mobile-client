package com.quare.bibleplanner.feature.logout.domain.usecase

internal class CurrentDeviceRevokedException(
    serverSessionState: String,
) : Exception(
        "Current device row disappeared from the synced list " +
            "(server_session_state=$serverSessionState); ending the local session",
    )
