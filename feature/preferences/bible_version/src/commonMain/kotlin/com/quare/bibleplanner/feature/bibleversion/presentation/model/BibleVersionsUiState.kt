package com.quare.bibleplanner.feature.bibleversion.presentation.model

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.utils.locale.Language

sealed interface BibleVersionsUiState {
    data object Loading : BibleVersionsUiState

    data class Success(
        val data: Map<Language, List<BibleModel>>,
    ) : BibleVersionsUiState

    data object Error : BibleVersionsUiState
}
