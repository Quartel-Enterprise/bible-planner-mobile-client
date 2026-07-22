package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

class GetDayStudyQuotaUseCase(
    private val repository: DayStudyRepository,
    private val bibleRepository: BibleRepository,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val languageCodeMapper: LanguageCodeMapper,
    private val getIntRemoteConfig: GetIntRemoteConfig,
) {
    suspend operator fun invoke(passages: List<PassageModel>): DayStudyQuotaModel = coroutineScope {
        val version = bibleRepository.getSelectedVersionIdFlow().first()
        val languageCode = languageCodeMapper.map(getAppLanguageFlow().first())
        val hasLocalStudyDeferred = async {
            repository.hasCachedStudy(
                passages = passages,
                version = version,
                languageCode = languageCode,
            )
        }
        val status = repository.getDayStudyStatus(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
        val hasLocalStudy = hasLocalStudyDeferred.await()
        if (status != null) {
            DayStudyQuotaModel(
                freeLimit = status.freeLimit,
                remainingFree = (status.freeLimit - status.usedCount).coerceAtLeast(0),
                isUnlockedForDay = status.isUnlocked || hasLocalStudy,
                hasLocalStudy = hasLocalStudy,
            )
        } else {
            val freeLimit = getIntRemoteConfig(
                key = FREE_LIMIT_KEY,
                default = FREE_LIMIT_FALLBACK,
            )
            DayStudyQuotaModel(
                freeLimit = freeLimit,
                remainingFree = freeLimit,
                isUnlockedForDay = hasLocalStudy,
                hasLocalStudy = hasLocalStudy,
            )
        }
    }

    companion object {
        private const val FREE_LIMIT_KEY = "ai_study_free_limit"
        private const val FREE_LIMIT_FALLBACK = 3
    }
}
