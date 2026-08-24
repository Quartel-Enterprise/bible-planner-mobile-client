package com.quare.bibleplanner.feature.read.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import org.jetbrains.compose.resources.StringResource

/** Chapter identity and the controls that stay on screen whatever the content is doing. */
data class ReadHeaderUiModel(
    val bookId: BookId,
    val bookStringResource: StringResource,
    val chapterNumber: Int,
    val isChapterRead: Boolean,
    val navigationSuggestions: ReadNavigationSuggestionsModel,
    /** The version's short id (ARC, WEB…): the full name does not fit the reader's chip. */
    val versionAbbreviation: Loadable<String>,
)
