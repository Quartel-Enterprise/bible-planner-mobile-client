package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import kotlinx.coroutines.flow.first

class HasCachedStudyUseCase(
    private val repository: DayStudyRepository,
    private val bibleRepository: BibleRepository,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val languageCodeMapper: LanguageCodeMapper,
) {
    suspend operator fun invoke(passages: List<PassageModel>): Boolean {
        val version = bibleRepository.getSelectedVersionIdFlow().first()
        val languageCode = languageCodeMapper.map(getAppLanguageFlow().first())
        return repository.hasCachedStudy(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
    }
}
