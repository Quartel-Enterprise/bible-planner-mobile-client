package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import org.jetbrains.compose.resources.getString

internal class GetListeningChapterTitleUseCase : GetListeningChapterTitle {
    override suspend fun invoke(chapter: ChapterLocationModel): String =
        "${getString(chapter.bookId.toBookNameResource())} ${chapter.chapterNumber}"
}
