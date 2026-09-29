package com.quare.bibleplanner.core.verseannotations.domain.usecase.impl

import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.core.verseannotations.domain.repository.AnnotatedVersionRepository
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveAnnotatedPassages
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveVersionAnnotationCounts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class ObserveVersionAnnotationCountsUseCase(
    private val annotatedVersionRepository: AnnotatedVersionRepository,
    private val observeAnnotatedPassages: ObserveAnnotatedPassages,
) : ObserveVersionAnnotationCounts {
    override fun invoke(): Flow<List<VersionAnnotationCount>> = annotatedVersionRepository
        .observeAnnotatedVersionIds()
        .flatMapLatest(::observeCounts)

    private fun observeCounts(versionIds: List<String>): Flow<List<VersionAnnotationCount>> =
        if (versionIds.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(versionIds.map(::observeCount)) { counts -> counts.filter { it.count > 0 } }
        }

    private fun observeCount(versionId: String): Flow<VersionAnnotationCount> = observeAnnotatedPassages(versionId)
        .map { passages ->
            VersionAnnotationCount(
                bibleVersionId = versionId,
                count = passages.size,
            )
        }
}
