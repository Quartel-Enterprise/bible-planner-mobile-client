package com.quare.bibleplanner.feature.shareverse.presentation.model

import com.quare.bibleplanner.ui.theme.font.ShareCardFont

data class ShareVerseUiState(
    val quote: String,
    val reference: String,
    val versionName: String,
    val background: ShareCardBackground,
    val font: ShareCardFont,
    val isReady: Boolean,
)
