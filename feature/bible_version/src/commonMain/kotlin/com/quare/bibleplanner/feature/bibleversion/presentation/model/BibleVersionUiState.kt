package com.quare.bibleplanner.feature.bibleversion.presentation.model

import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersion

data class BibleVersionUiState(
    val versions: List<BibleVersion> = emptyList(),
)
