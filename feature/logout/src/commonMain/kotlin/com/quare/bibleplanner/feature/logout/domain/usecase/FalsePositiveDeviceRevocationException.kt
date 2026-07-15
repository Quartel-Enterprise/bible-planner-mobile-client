package com.quare.bibleplanner.feature.logout.domain.usecase

internal class FalsePositiveDeviceRevocationException :
    Exception(
        "Local device row vanished while the server session was still active; " +
            "kept the user signed in instead of ending the session",
    )
