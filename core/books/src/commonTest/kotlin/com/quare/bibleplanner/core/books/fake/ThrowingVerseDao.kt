package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import kotlinx.coroutines.flow.Flow

internal open class ThrowingVerseDao : VerseDao {
    override fun getVersesByChapterIdFlow(chapterId: Long): Flow<List<VerseEntity>> = error("Unexpected call")

    override suspend fun getVersesByChapterId(chapterId: Long): List<VerseEntity> = error("Unexpected call")

    override suspend fun getVersesWithTextsByChapterId(chapterId: Long): List<VerseWithTexts> = error("Unexpected call")

    override fun getVersesWithTextsByChapterIdFlow(chapterId: Long): Flow<List<VerseWithTexts>> =
        error("Unexpected call")

    override fun getVerseById(verseId: Long): Flow<VerseEntity?> = error("Unexpected call")

    override suspend fun getVerseByIdSuspend(verseId: Long): VerseEntity? = error("Unexpected call")

    override suspend fun getVerseByChapterIdAndNumber(
        chapterId: Long,
        verseNumber: Int,
    ): VerseEntity? = error("Unexpected call")

    override suspend fun upsertVerse(verse: VerseEntity): Long = error("Unexpected call")

    override suspend fun upsertVerses(verses: List<VerseEntity>): List<Long> = error("Unexpected call")

    override suspend fun upsertVerseTexts(verseTexts: List<VerseTextEntity>): List<Long> = error("Unexpected call")

    override suspend fun updateVerse(verse: VerseEntity): Unit = error("Unexpected call")

    override suspend fun updateVerseReadStatus(
        verseId: Long,
        isRead: Boolean,
    ): Unit = error("Unexpected call")

    override suspend fun updateVersesReadStatusByChapter(
        chapterId: Long,
        isRead: Boolean,
    ): Unit = error("Unexpected call")

    override suspend fun updateVersesReadStatusByBook(
        bookId: String,
        isRead: Boolean,
    ): Unit = error("Unexpected call")

    override suspend fun updateVerseReadStatusRange(
        chapterId: Long,
        startVerse: Int,
        endVerse: Int,
        isRead: Boolean,
        updatedAt: Long,
    ): Unit = error("Unexpected call")

    override fun getPendingReadSyncVersesFlow(): Flow<List<PendingVerseRead>> = error("Unexpected call")

    override suspend fun getPendingReadSyncVerses(): List<PendingVerseRead> = error("Unexpected call")

    override suspend fun markVerseReadSynced(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        syncedUpdatedAt: Long,
    ): Unit = error("Unexpected call")

    override suspend fun applyRemoteVerseRead(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        isRead: Boolean,
        remoteUpdatedAt: Long,
    ): Unit = error("Unexpected call")

    override suspend fun cascadeChapterReadToVerses(
        bookId: String,
        chapterNumber: Int,
        isRead: Boolean,
    ): Unit = error("Unexpected call")

    override suspend fun markLegacyVerseReadsPending(now: Long): Unit = error("Unexpected call")

    override suspend fun clearAllVerseReadSync(): Unit = error("Unexpected call")

    override suspend fun resetAllVerseReadsForSync(now: Long): Unit = error("Unexpected call")

    override suspend fun deleteVerse(verseId: Long): Unit = error("Unexpected call")

    override suspend fun deleteVersesByChapterId(chapterId: Long): Unit = error("Unexpected call")

    override suspend fun countVersesByChapterAndVersion(
        chapterId: Long,
        versionId: String,
    ): Int = error("Unexpected call")

    override suspend fun countChaptersWithVersesByVersion(versionId: String): Int = error("Unexpected call")

    override fun countChaptersWithVersesByVersionFlow(versionId: String): Flow<Int> = error("Unexpected call")

    override fun getDownloadedChaptersPerVersionFlow(): Flow<List<VersionChapterCount>> = error("Unexpected call")

    override suspend fun deleteVerseTextsByVersion(versionId: String): Unit = error("Unexpected call")
}
