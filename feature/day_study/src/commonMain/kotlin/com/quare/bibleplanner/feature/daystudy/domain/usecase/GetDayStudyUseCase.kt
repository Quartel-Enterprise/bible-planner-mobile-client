package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GetDayStudyUseCase(
    private val repository: DayStudyRepository,
    private val bibleRepository: BibleRepository,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val languageCodeMapper: LanguageCodeMapper,
) {
    operator fun invoke(passages: List<PassageModel>): Flow<DayStudyGenerationEventModel> = flow {
        val version = bibleRepository.getSelectedVersionIdFlow().first()
        val languageCode = languageCodeMapper.map(getAppLanguageFlow().first())
        emitAll(
            repository.getDayStudy(
                passages = passages,
                version = version,
                languageCode = languageCode,
            ),
        )
    }
}
