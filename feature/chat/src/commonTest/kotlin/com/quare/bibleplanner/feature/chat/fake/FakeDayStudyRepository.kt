package com.quare.bibleplanner.feature.chat.fake

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import kotlinx.coroutines.flow.Flow

internal class FakeDayStudyRepository(
    private val isCached: Boolean,
    private val study: Flow<DayStudyGenerationEventModel>,
) : DayStudyRepository {
    val cacheLookups: MutableList<Pair<String, String>> = mutableListOf()

    override fun getDayStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Flow<DayStudyGenerationEventModel> = study

    override suspend fun getDayStudyStatus(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyStatusModel? = error("unused")

    override suspend fun hasCachedStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Boolean {
        cacheLookups += version to languageCode
        return isCached
    }
}
