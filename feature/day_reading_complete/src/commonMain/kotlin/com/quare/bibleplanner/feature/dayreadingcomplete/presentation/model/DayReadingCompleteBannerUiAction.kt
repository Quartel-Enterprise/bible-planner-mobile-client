package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface DayReadingCompleteBannerUiAction {
    data class ShowSnackBar(
        val message: StringResource,
    ) : DayReadingCompleteBannerUiAction

    data object Dismiss : DayReadingCompleteBannerUiAction
}
