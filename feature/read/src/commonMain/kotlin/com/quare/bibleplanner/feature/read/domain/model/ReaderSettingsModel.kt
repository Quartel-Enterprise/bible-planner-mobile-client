package com.quare.bibleplanner.feature.read.domain.model

import com.quare.bibleplanner.ui.theme.font.ReaderFont

// Why: the ruler and the focused verse are never on together (they compete for attention);
// SetReaderFocusAid enforces it.
data class ReaderSettingsModel(
    val fontSizeSp: Float,
    val font: ReaderFont,
    val isRulerEnabled: Boolean,
    val rulerLines: Int,
    val isFocusedVerseEnabled: Boolean,
    val isVerticalReadingEnabled: Boolean,
    val isNoteIconEnabled: Boolean,
)
