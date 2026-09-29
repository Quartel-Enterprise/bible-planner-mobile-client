package com.quare.bibleplanner.feature.verse.annotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import kotlinx.coroutines.flow.Flow

internal fun interface ObserveOtherVersionAnnotationCounts {
    operator fun invoke(): Flow<List<VersionAnnotationCount>>
}
