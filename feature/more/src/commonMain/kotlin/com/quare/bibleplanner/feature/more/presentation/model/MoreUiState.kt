package com.quare.bibleplanner.feature.more.presentation.model

import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

internal sealed interface MoreUiState {
    data object Loading : MoreUiState

    data class Loaded(
        val themeSubtitle: StringResource,
        val planStartDate: LocalDate?,
        val currentDate: LocalDate,
        val isFreeUser: Boolean,
        val isInstagramLinkVisible: Boolean,
        val shouldShowDonateOption: Boolean,
        val headerRes: StringResource?,
    ) : MoreUiState
}
