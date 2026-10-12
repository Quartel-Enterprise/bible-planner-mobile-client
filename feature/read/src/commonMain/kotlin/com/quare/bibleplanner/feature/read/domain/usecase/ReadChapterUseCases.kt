package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.books.domain.usecase.ToggleWholeChapterReadStatus

data class ReadChapterUseCases(
    val toggleWholeChapterReadStatus: ToggleWholeChapterReadStatus,
    val isWholeChapterRead: IsWholeChapterRead,
)
