package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleSelectionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleVersionRepository
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBibleVersionsByLanguageUseCase(
    private val repository: BibleVersionRepository,
) {
    operator fun invoke(): Flow<Map<Language, List<BibleSelectionModel>>> {
        val selectedLanguage = getCurrentLanguage()
        return repository
            .getSelectableBibleVersions()
            .map { selectionModels ->
                selectionModels
                    .groupBy { it.version.language }
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<Language, List<BibleSelectionModel>>> {
                            it.key == selectedLanguage
                        }.thenBy { it.key.name },
                    ).associate { it.key to it.value }
            }
    }
}
