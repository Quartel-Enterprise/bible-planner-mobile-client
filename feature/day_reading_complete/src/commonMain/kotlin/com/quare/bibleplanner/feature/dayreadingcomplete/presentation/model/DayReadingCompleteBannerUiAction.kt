package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

sealed interface DayReadingCompleteBannerUiAction {
    data class ShowSnackBar(
        val message: DayReadingCompleteMessage,
    ) : DayReadingCompleteBannerUiAction

    data object Dismiss : DayReadingCompleteBannerUiAction
}
