package com.quare.bibleplanner.feature.day.presentation.model

import kotlinx.datetime.Month

// Why: the month stays a Month so each UI (Compose, SwiftUI) picks its own localized name.
internal data class DatePresentationModel(
    val day: Int,
    val month: Month,
    val year: Int,
    val minute: String,
    val hour: String,
)
