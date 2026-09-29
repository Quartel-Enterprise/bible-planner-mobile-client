package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import kotlinx.coroutines.flow.Flow

fun interface ObserveVersionAnnotationCounts {
    operator fun invoke(): Flow<List<VersionAnnotationCount>>
}
