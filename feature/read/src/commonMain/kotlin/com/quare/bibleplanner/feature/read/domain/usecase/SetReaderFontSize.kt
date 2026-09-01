package com.quare.bibleplanner.feature.read.domain.usecase

fun interface SetReaderFontSize {
    suspend operator fun invoke(fontSizeSp: Float)
}
