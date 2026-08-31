package com.quare.bibleplanner.feature.readingplan.presentation.model

sealed interface ReadingPlanUiAction {
    data object ShowNoProgressToDelete : ReadingPlanUiAction

    data class OpenLink(
        val url: String,
    ) : ReadingPlanUiAction
}
