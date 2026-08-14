package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.domain.usecase.GetChatSuggestions
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.QaModel
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.HasCachedStudyUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

class GetChatSuggestionsUseCase(
    private val hasCachedStudy: HasCachedStudyUseCase,
    private val getDayStudy: GetDayStudyUseCase,
) : GetChatSuggestions {
    override suspend fun invoke(passages: List<PassageModel>): List<String> {
        if (passages.isEmpty()) return emptyList()
        return suspendRunCatching {
            if (!hasCachedStudy(passages)) return@suspendRunCatching emptyList()
            getDayStudy(passages)
                .mapNotNull { event -> (event as? DayStudyGenerationEventModel.Completed)?.study }
                .first()
                .commonQuestions
                .map(QaModel::question)
        }.getOrDefault(emptyList())
    }
}
