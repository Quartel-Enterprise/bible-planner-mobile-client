package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.provider.room.dao.ChapterDao
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

internal open class ThrowingChapterDao : ChapterDao {
    override fun getChaptersByBookIdFlow(bookId: String): Flow<List<ChapterEntity>> = error("Unexpected call")

    override suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity> = error("Unexpected call")

    override fun getChapterByIdFlow(chapterId: Long): Flow<ChapterEntity?> = error("Unexpected call")

    override suspend fun getChapterById(chapterId: Long): ChapterEntity? = error("Unexpected call")

    override suspend fun getChapterByBookIdAndNumber(
        bookId: String,
        chapterNumber: Int,
    ): ChapterEntity? = error("Unexpected call")

    override suspend fun insertChapter(chapter: ChapterEntity): Long = error("Unexpected call")

    override suspend fun insertChapters(chapters: List<ChapterEntity>): List<Long> = error("Unexpected call")

    override suspend fun updateChapter(chapter: ChapterEntity): Unit = error("Unexpected call")

    override suspend fun updateChapterReadStatus(
        chapterId: Long,
        isRead: Boolean,
        updatedAt: Long,
    ): Unit = error("Unexpected call")

    override suspend fun updateChaptersReadStatusByBook(
        bookId: String,
        isRead: Boolean,
        updatedAt: Long,
    ): Unit = error("Unexpected call")

    override fun getPendingReadSyncChaptersFlow(): Flow<List<ChapterEntity>> = error("Unexpected call")

    override suspend fun getPendingReadSyncChapters(): List<ChapterEntity> = error("Unexpected call")

    override suspend fun markChapterReadSynced(
        bookId: String,
        chapterNumber: Int,
        syncedUpdatedAt: Long,
    ): Unit = error("Unexpected call")

    override suspend fun applyRemoteChapterRead(
        bookId: String,
        chapterNumber: Int,
        isRead: Boolean,
        remoteUpdatedAt: Long,
    ): Int = error("Unexpected call")

    override suspend fun markLegacyChapterReadsPending(now: Long): Unit = error("Unexpected call")

    override suspend fun clearAllChapterReadSync(): Unit = error("Unexpected call")

    override suspend fun resetAllChapterReadsForSync(now: Long): Unit = error("Unexpected call")

    override suspend fun deleteChapter(chapterId: Long): Unit = error("Unexpected call")

    override suspend fun deleteChaptersByBookId(bookId: String): Unit = error("Unexpected call")

    override suspend fun deleteAllChapters(): Unit = error("Unexpected call")
}
