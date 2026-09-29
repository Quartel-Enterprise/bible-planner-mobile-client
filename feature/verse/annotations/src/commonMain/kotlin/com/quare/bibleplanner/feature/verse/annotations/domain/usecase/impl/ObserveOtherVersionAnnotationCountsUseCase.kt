package com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVersionAnnotationCounts
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveOtherVersionAnnotationCounts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ObserveOtherVersionAnnotationCountsUseCase(
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val observeVersionAnnotationCounts: ObserveVersionAnnotationCounts,
) : ObserveOtherVersionAnnotationCounts {
    override fun invoke(): Flow<List<VersionAnnotationCount>> = combine(
        getSelectedVersionIdFlow(),
        observeVersionAnnotationCounts(),
    ) { selectedVersionId, counts ->
        counts.filter { it.bibleVersionId != selectedVersionId }
    }
}
