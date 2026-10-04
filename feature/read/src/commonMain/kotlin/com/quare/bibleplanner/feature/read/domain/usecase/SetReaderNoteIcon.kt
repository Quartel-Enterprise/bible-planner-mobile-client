package com.quare.bibleplanner.feature.read.domain.usecase

fun interface SetReaderNoteIcon {
    suspend operator fun invoke(isEnabled: Boolean)
}
