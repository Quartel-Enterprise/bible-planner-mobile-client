package com.quare.bibleplanner.feature.daystudy.fake

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeDayStudyRepository(
    var hasCached: Boolean,
    var status: DayStudyStatusModel?,
    var statusError: Throwable?,
    var events: List<DayStudyGenerationEventModel>,
) : DayStudyRepository {
    override fun getDayStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Flow<DayStudyGenerationEventModel> = flowOf(*events.toTypedArray())

    override suspend fun getDayStudyStatus(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyStatusModel? {
        statusError?.let { throw it }
        return status
    }

    override suspend fun hasCachedStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Boolean = hasCached
}
