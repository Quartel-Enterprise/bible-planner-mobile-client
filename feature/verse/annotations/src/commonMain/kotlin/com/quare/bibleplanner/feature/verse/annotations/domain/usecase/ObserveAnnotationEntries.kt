package com.quare.bibleplanner.feature.verse.annotations.domain.usecase

import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import kotlinx.coroutines.flow.Flow

internal fun interface ObserveAnnotationEntries {
    operator fun invoke(): Flow<List<AnnotationEntry>>
}
