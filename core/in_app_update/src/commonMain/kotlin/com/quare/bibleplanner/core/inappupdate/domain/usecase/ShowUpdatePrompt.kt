package com.quare.bibleplanner.core.inappupdate.domain.usecase

import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability

fun interface ShowUpdatePrompt {
    suspend operator fun invoke(
        availability: UpdateAvailability.Available,
        source: String,
    )
}
