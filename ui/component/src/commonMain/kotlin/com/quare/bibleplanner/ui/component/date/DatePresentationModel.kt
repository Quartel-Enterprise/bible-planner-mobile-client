package com.quare.bibleplanner.ui.component.date

import org.jetbrains.compose.resources.StringResource

data class DatePresentationModel(
    val day: Int,
    val month: StringResource,
    val year: Int,
    val minute: String,
    val hour: String,
)
