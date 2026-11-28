package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import software.quare.loadable.Loadable

data class ReadHeaderUiModel(
    val bookId: BookId,
    val chapterNumber: Int,
    val isChapterRead: Boolean,
    val navigationSuggestions: ReadNavigationSuggestionsModel,
    val versionAbbreviation: Loadable<String>,
)
