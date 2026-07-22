package com.quare.bibleplanner.feature.inappupdate.domain.usecase

import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability

fun interface ShowUpdatePrompt {
    suspend operator fun invoke(
        availability: UpdateAvailability.Available,
        source: String,
    )
}
