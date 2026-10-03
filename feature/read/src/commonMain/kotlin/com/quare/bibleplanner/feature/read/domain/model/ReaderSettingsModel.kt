package com.quare.bibleplanner.feature.read.domain.model

import com.quare.bibleplanner.ui.theme.font.ReaderFont

data class ReaderSettingsModel(
    val fontSizeSp: Float,
    val font: ReaderFont,
    val isRulerEnabled: Boolean,
    val rulerLines: Int,
    val isFocusedVerseEnabled: Boolean,
    val isVerticalReadingEnabled: Boolean,
)
