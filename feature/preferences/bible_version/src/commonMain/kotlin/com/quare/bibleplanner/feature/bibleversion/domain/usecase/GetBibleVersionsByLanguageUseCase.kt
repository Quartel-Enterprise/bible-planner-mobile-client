package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBibleVersionsByLanguageUseCase(
    private val repository: BibleRepository,
    private val getAppLanguageFlow: GetAppLanguageFlow,
) {
    operator fun invoke(): Flow<Map<Language, List<BibleModel>>> =
        combine(getAppLanguageFlow(), repository.getBiblesFlow()) { selectedLanguage, selectionModels ->
            selectionModels
                .groupBy { it.version.language }
                .entries
                .sortedWith(
                    compareByDescending<Map.Entry<Language, List<BibleModel>>> {
                        it.key == selectedLanguage
                    }.thenBy { it.key.name },
                ).associate { it.key to it.value }
        }
}
