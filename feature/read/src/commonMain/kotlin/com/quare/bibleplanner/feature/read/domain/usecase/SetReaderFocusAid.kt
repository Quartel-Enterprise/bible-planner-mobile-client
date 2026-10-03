package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid

fun interface SetReaderFocusAid {
    suspend operator fun invoke(focusAid: ReaderFocusAid)
}
