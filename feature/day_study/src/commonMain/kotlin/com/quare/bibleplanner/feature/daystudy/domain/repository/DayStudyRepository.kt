package com.quare.bibleplanner.feature.daystudy.domain.repository

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import kotlinx.coroutines.flow.Flow

interface DayStudyRepository {
    fun getDayStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Flow<DayStudyGenerationEventModel>

    suspend fun getDayStudyStatus(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyStatusModel?

    suspend fun hasCachedStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Boolean
}
