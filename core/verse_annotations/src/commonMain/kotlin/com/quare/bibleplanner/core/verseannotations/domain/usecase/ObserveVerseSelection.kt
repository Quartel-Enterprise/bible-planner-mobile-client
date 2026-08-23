package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseSelection
import kotlinx.coroutines.flow.StateFlow

fun interface ObserveVerseSelection {
    operator fun invoke(): StateFlow<VerseSelection?>
}
