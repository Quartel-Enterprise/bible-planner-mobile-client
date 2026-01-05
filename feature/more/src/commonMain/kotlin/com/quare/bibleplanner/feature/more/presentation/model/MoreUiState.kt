package com.quare.bibleplanner.feature.more.presentation.model

import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

internal data class MoreUiState(
    val themeSubtitle: StringResource,
    val planStartDate: LocalDate?,
    val currentDate: LocalDate,
    val isFreeUser: Boolean,
    val isInstagramLinkVisible: Boolean,
    val shouldShowDonateOption: Boolean,
    val headerRes: StringResource?,
)
