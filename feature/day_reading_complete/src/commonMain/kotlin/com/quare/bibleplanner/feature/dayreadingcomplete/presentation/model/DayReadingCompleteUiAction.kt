package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface DayReadingCompleteUiAction {
    data class ShowSnackBar(
        val message: StringResource,
    ) : DayReadingCompleteUiAction
}
