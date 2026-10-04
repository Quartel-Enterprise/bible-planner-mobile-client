package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveIsChapterListeningEnabled {
    operator fun invoke(): Flow<Boolean>
}
