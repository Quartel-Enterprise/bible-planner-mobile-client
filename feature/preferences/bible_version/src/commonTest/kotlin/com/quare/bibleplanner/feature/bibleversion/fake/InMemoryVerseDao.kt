package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity

internal class InMemoryVerseDao(
    private val verses: List<VerseEntity> = emptyList(),
    private val downloadedChapterIds: List<Long> = emptyList(),
    private val chaptersWithVerses: Int = 0,
) : ThrowingVerseDao() {
    val savedVerseTextBatches = mutableListOf<List<VerseTextEntity>>()

    override suspend fun countChaptersWithVersesByVersion(versionId: String): Int = chaptersWithVerses

    override suspend fun getDownloadedChapterIds(
        versionId: String,
        chapterIds: List<Long>,
    ): List<Long> = downloadedChapterIds.filter { it in chapterIds }

    override suspend fun getVersesByChapterIds(chapterIds: List<Long>): List<VerseEntity> =
        verses.filter { it.chapterId in chapterIds }

    override suspend fun upsertVerseTexts(verseTexts: List<VerseTextEntity>) {
        savedVerseTextBatches += verseTexts
    }
}
