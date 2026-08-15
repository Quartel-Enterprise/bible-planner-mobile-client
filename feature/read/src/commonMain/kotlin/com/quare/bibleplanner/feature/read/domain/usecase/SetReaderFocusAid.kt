package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid

fun interface SetReaderFocusAid {
    /** Turning one aid on turns the other off: they compete for the same attention. */
    suspend operator fun invoke(focusAid: ReaderFocusAid)
}
