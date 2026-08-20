package com.quare.bibleplanner.feature.read.domain.model

import com.quare.bibleplanner.ui.theme.font.ReaderFont

/**
 * How the user reads: typography plus the two focus aids. The ruler and the focused verse are never
 * on at the same time — they compete for the same attention — which
 * [com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid] enforces.
 */
data class ReaderSettingsModel(
    val fontSizeSp: Float,
    val font: ReaderFont,
    val isRulerEnabled: Boolean,
    val rulerLines: Int,
    val isFocusedVerseEnabled: Boolean,
    val isVerticalReadingEnabled: Boolean,
)
