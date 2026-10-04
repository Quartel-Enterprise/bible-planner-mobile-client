package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetAdjacentListeningChapter
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetPreviousChapter

internal class GetAdjacentListeningChapterUseCase(
    private val getNextChapter: GetNextChapter,
    private val getPreviousChapter: GetPreviousChapter,
) : GetAdjacentListeningChapter {
    override suspend fun invoke(
        chapter: ChapterLocationModel,
        direction: ChapterDirectionModel,
        shouldForceCanonOrder: Boolean,
    ): ChapterLocationModel? {
        val adjacent = when (direction) {
            ChapterDirectionModel.PREVIOUS -> getPreviousChapter(
                bookId = chapter.bookId,
                chapterNumber = chapter.chapterNumber,
                shouldForceCanonOrder = shouldForceCanonOrder,
            )

            ChapterDirectionModel.NEXT -> getNextChapter(
                bookId = chapter.bookId,
                chapterNumber = chapter.chapterNumber,
                shouldForceCanonOrder = shouldForceCanonOrder,
            )
        }
        return adjacent?.let(::toChapter)
    }

    private fun toChapter(suggestion: ReadNavigationSuggestionModel): ChapterLocationModel = ChapterLocationModel(
        bookId = suggestion.bookId,
        chapterNumber = suggestion.chapterNumber,
    )
}
