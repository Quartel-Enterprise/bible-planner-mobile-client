package com.quare.bibleplanner.feature.readingplan.presentation.mapper

import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.feature.readingplan.domain.model.ResumeReadTarget

internal fun ResumeReadTarget.toReadNavRoute(): ReadNavRoute = ReadNavRoute(
    bookId = bookId.name,
    chapterNumber = chapterNumber,
    isChapterRead = isChapterRead,
    isFromBookDetails = false,
    targetVerseNumbers = emptyList(),
)
