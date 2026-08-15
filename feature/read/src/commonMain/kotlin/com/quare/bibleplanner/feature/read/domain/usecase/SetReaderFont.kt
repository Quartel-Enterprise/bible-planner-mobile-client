package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.ui.theme.font.ReaderFont

fun interface SetReaderFont {
    suspend operator fun invoke(font: ReaderFont)
}
