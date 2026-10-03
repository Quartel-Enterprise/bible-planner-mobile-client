package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number")
    fun getChaptersByBookIdFlow(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number")
    suspend fun getChaptersByBookId(bookId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterByIdFlow(chapterId: Long): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND number = :chapterNumber")
    suspend fun getChapterByBookIdAndNumber(
        bookId: String,
        chapterNumber: Int,
    ): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>): List<Long>

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query(
        "UPDATE chapters SET isRead = :isRead, readUpdatedAt = :updatedAt, isReadPendingSync = 1 " +
            "WHERE id = :chapterId AND isRead <> :isRead",
    )
    suspend fun updateChapterReadStatus(
        chapterId: Long,
        isRead: Boolean,
        updatedAt: Long,
    )

    @Query(
        "UPDATE chapters SET isRead = :isRead, readUpdatedAt = :updatedAt, isReadPendingSync = 1 " +
            "WHERE bookId = :bookId AND isRead <> :isRead",
    )
    suspend fun updateChaptersReadStatusByBook(
        bookId: String,
        isRead: Boolean,
        updatedAt: Long,
    )

    @Query("SELECT * FROM chapters WHERE isReadPendingSync = 1")
    fun getPendingReadSyncChaptersFlow(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE isReadPendingSync = 1")
    suspend fun getPendingReadSyncChapters(): List<ChapterEntity>

    @Query(
        "UPDATE chapters SET isReadPendingSync = 0 " +
            "WHERE bookId = :bookId AND number = :chapterNumber AND readUpdatedAt = :syncedUpdatedAt",
    )
    suspend fun markChapterReadSynced(
        bookId: String,
        chapterNumber: Int,
        syncedUpdatedAt: Long,
    )

    @Query(
        "UPDATE chapters SET isRead = :isRead, readUpdatedAt = :remoteUpdatedAt " +
            "WHERE bookId = :bookId AND number = :chapterNumber AND isReadPendingSync = 0 " +
            "AND (readUpdatedAt IS NULL OR readUpdatedAt < :remoteUpdatedAt)",
    )
    suspend fun applyRemoteChapterRead(
        bookId: String,
        chapterNumber: Int,
        isRead: Boolean,
        remoteUpdatedAt: Long,
    ): Int

    @Query(
        "UPDATE chapters SET isReadPendingSync = 1, readUpdatedAt = :now " +
            "WHERE isRead = 1 AND readUpdatedAt IS NULL",
    )
    suspend fun markLegacyChapterReadsPending(now: Long)

    @Query("UPDATE chapters SET isRead = 0, readUpdatedAt = NULL, isReadPendingSync = 0")
    suspend fun clearAllChapterReadSync()

    @Query(
        "UPDATE chapters SET isRead = 0, " +
            "isReadPendingSync = CASE WHEN readUpdatedAt IS NOT NULL THEN 1 ELSE isReadPendingSync END, " +
            "readUpdatedAt = CASE WHEN readUpdatedAt IS NOT NULL THEN :now ELSE readUpdatedAt END",
    )
    suspend fun resetAllChapterReadsForSync(now: Long)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapter(chapterId: Long)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String)

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()
}
