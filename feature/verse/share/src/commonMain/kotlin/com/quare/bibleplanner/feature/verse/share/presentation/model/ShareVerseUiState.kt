package com.quare.bibleplanner.feature.verse.share.presentation.model

import com.quare.bibleplanner.ui.theme.font.ShareCardFont

data class ShareVerseUiState(
    val quote: String,
    val reference: String,
    val versionAbbreviation: String,
    val background: ShareCardBackground,
    val font: ShareCardFont,
    val isReady: Boolean,
)
