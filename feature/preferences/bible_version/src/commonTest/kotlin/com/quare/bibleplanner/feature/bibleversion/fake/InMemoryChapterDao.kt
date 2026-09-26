package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity

internal class InMemoryChapterDao(
    private val chapters: List<ChapterEntity>,
) : ThrowingChapterDao() {
    override suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity> =
        chapters.filter { it.bookId == bookId }
}
