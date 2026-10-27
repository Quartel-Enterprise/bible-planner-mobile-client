package com.quare.bibleplanner.feature.verse.share.presentation.model

import com.quare.bibleplanner.ui.theme.font.ShareCardFont

data class ShareVerseUiState(
    val quote: String,
    val reference: String,
    val versionAbbreviation: String,
    val background: ShareCardBackgroundUiModel,
    val customColor: ShareCardCustomColor,
    val isColorPickerOpen: Boolean,
    val font: ShareCardFont,
    val isWatermarkShown: Boolean,
    val streakDays: Int,
    val isStreakShown: Boolean,
    val isProUser: Boolean,
    val isReady: Boolean,
)
