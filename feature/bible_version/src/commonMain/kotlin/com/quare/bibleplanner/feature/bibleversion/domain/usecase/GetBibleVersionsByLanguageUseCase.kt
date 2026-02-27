package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBibleVersionsByLanguageUseCase(
    private val repository: BibleRepository,
) {
    operator fun invoke(): Flow<Map<Language, List<BibleModel>>> {
        val selectedLanguage = getCurrentLanguage()
        return repository
            .getBiblesFlow()
            .map { selectionModels ->
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
}
