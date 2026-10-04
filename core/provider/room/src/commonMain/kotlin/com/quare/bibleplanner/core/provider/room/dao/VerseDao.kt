package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    fun getVersesByChapterIdFlow(chapterId: Long): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    suspend fun getVersesByChapterId(chapterId: Long): List<VerseEntity>

    @Query("SELECT * FROM verses WHERE chapterId IN (:chapterIds) ORDER BY chapterId, number")
    suspend fun getVersesByChapterIds(chapterIds: List<Long>): List<VerseEntity>

    @Transaction
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    suspend fun getVersesWithTextsByChapterId(chapterId: Long): List<VerseWithTexts>

    @Transaction
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    fun getVersesWithTextsByChapterIdFlow(chapterId: Long): Flow<List<VerseWithTexts>>

    @Query("SELECT * FROM verses WHERE id = :verseId")
    fun getVerseById(verseId: Long): Flow<VerseEntity?>

    @Query("SELECT * FROM verses WHERE id = :verseId")
    suspend fun getVerseByIdSuspend(verseId: Long): VerseEntity?

    @Query("SELECT * FROM verses WHERE chapterId = :chapterId AND number = :verseNumber")
    suspend fun getVerseByChapterIdAndNumber(
        chapterId: Long,
        verseNumber: Int,
    ): VerseEntity?

    @Upsert
    suspend fun upsertVerse(verse: VerseEntity): Long

    @Upsert
    suspend fun upsertVerses(verses: List<VerseEntity>): List<Long>

    /*
     * Why: returns nothing on purpose; Room row ids cost a last_insert_rowid() round trip per
     * row, and a Bible download writes tens of thousands of rows nobody reads ids for.
     */
    @Upsert
    suspend fun upsertVerseTexts(verseTexts: List<VerseTextEntity>)

    @Update
    suspend fun updateVerse(verse: VerseEntity)

    @Query(
        "UPDATE verses SET isRead = :isRead WHERE chapterId = (SELECT chapterId FROM verses WHERE id = :verseId) AND number = (SELECT number FROM verses WHERE id = :verseId)",
    )
    suspend fun updateVerseReadStatus(
        verseId: Long,
        isRead: Boolean,
    )

    @Query("UPDATE verses SET isRead = :isRead WHERE chapterId = :chapterId")
    suspend fun updateVersesReadStatusByChapter(
        chapterId: Long,
        isRead: Boolean,
    )

    @Query("UPDATE verses SET isRead = :isRead WHERE chapterId IN (SELECT id FROM chapters WHERE bookId = :bookId)")
    suspend fun updateVersesReadStatusByBook(
        bookId: String,
        isRead: Boolean,
    )

    /*
     * Why: isRead <> :isRead keeps verses already in that state out of the write; otherwise they
     * would go pending and cost a push and a realtime broadcast for an unchanged value.
     */
    @Query(
        "UPDATE verses SET isRead = :isRead, readUpdatedAt = :updatedAt, isReadPendingSync = 1 " +
            "WHERE chapterId = :chapterId AND number BETWEEN :startVerse AND :endVerse " +
            "AND isRead <> :isRead",
    )
    suspend fun updateVerseReadStatusRange(
        chapterId: Long,
        startVerse: Int,
        endVerse: Int,
        isRead: Boolean,
        updatedAt: Long,
    )

    @Query(
        "SELECT c.bookId AS bookId, c.number AS chapterNumber, v.number AS verseNumber, " +
            "v.isRead AS isRead, v.readUpdatedAt AS readUpdatedAt " +
            "FROM verses v INNER JOIN chapters c ON v.chapterId = c.id WHERE v.isReadPendingSync = 1",
    )
    fun getPendingReadSyncVersesFlow(): Flow<List<PendingVerseRead>>

    @Query(
        "SELECT c.bookId AS bookId, c.number AS chapterNumber, v.number AS verseNumber, " +
            "v.isRead AS isRead, v.readUpdatedAt AS readUpdatedAt " +
            "FROM verses v INNER JOIN chapters c ON v.chapterId = c.id WHERE v.isReadPendingSync = 1",
    )
    suspend fun getPendingReadSyncVerses(): List<PendingVerseRead>

    @Query(
        "UPDATE verses SET isReadPendingSync = 0 " +
            "WHERE chapterId = (SELECT id FROM chapters WHERE bookId = :bookId AND number = :chapterNumber) " +
            "AND number = :verseNumber AND readUpdatedAt = :syncedUpdatedAt",
    )
    suspend fun markVerseReadSynced(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        syncedUpdatedAt: Long,
    )

    @Query(
        "UPDATE verses SET isRead = :isRead, readUpdatedAt = :remoteUpdatedAt " +
            "WHERE chapterId = (SELECT id FROM chapters WHERE bookId = :bookId AND number = :chapterNumber) " +
            "AND number = :verseNumber AND isReadPendingSync = 0 " +
            "AND (readUpdatedAt IS NULL OR readUpdatedAt < :remoteUpdatedAt)",
    )
    suspend fun applyRemoteVerseRead(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        isRead: Boolean,
        remoteUpdatedAt: Long,
    )

    /*
     * Why: the chapter screen derives its checkmark from verses.all { isRead }, so a remote
     * whole-chapter read cascades to verses; verses with a pending local range edit are skipped.
     */
    @Query(
        "UPDATE verses SET isRead = :isRead " +
            "WHERE chapterId = (SELECT id FROM chapters WHERE bookId = :bookId AND number = :chapterNumber) " +
            "AND isReadPendingSync = 0",
    )
    suspend fun cascadeChapterReadToVerses(
        bookId: String,
        chapterNumber: Int,
        isRead: Boolean,
    )

    /*
     * Why: only verses inside a chapter not itself fully read are marked, so whole-chapter
     * legacy reads stay at chapter granularity.
     */
    @Query(
        "UPDATE verses SET isReadPendingSync = 1, readUpdatedAt = :now " +
            "WHERE isRead = 1 AND readUpdatedAt IS NULL " +
            "AND chapterId IN (SELECT id FROM chapters WHERE isRead = 0)",
    )
    suspend fun markLegacyVerseReadsPending(now: Long)

    // Why: logout wipe, so it clears read state without scheduling a push.
    @Query("UPDATE verses SET isRead = 0, readUpdatedAt = NULL, isReadPendingSync = 0")
    suspend fun clearAllVerseReadSync()

    /*
     * Why: delete-progress wipe; only verses that already had a remote row (readUpdatedAt not
     * null) are flagged pending, so the deletion propagates to other devices.
     */
    @Query(
        "UPDATE verses SET isRead = 0, " +
            "isReadPendingSync = CASE WHEN readUpdatedAt IS NOT NULL THEN 1 ELSE isReadPendingSync END, " +
            "readUpdatedAt = CASE WHEN readUpdatedAt IS NOT NULL THEN :now ELSE readUpdatedAt END",
    )
    suspend fun resetAllVerseReadsForSync(now: Long)

    @Query("DELETE FROM verses WHERE id = :verseId")
    suspend fun deleteVerse(verseId: Long)

    @Query("DELETE FROM verses WHERE chapterId = :chapterId")
    suspend fun deleteVersesByChapterId(chapterId: Long)

    @Query(
        "SELECT COUNT(*) FROM verse_texts WHERE bibleVersionId = :versionId AND verseId IN (SELECT id FROM verses WHERE chapterId = :chapterId)",
    )
    suspend fun countVersesByChapterAndVersion(
        chapterId: Long,
        versionId: String,
    ): Int

    @Query(
        "SELECT COUNT(DISTINCT chapterId) FROM verses INNER JOIN verse_texts ON verses.id = verse_texts.verseId WHERE verse_texts.bibleVersionId = :versionId",
    )
    suspend fun countChaptersWithVersesByVersion(versionId: String): Int

    @Query(
        "SELECT verse_texts.bibleVersionId, COUNT(DISTINCT verses.chapterId) AS downloadedChapters FROM verse_texts INNER JOIN verses ON verses.id = verse_texts.verseId GROUP BY verse_texts.bibleVersionId",
    )
    suspend fun getDownloadedChaptersPerVersion(): List<VersionChapterCount>

    // Why: checking a whole book at once keeps the download from paying one round trip per chapter.
    @Query(
        "SELECT DISTINCT verses.chapterId FROM verse_texts INNER JOIN verses ON verses.id = verse_texts.verseId WHERE verse_texts.bibleVersionId = :versionId AND verses.chapterId IN (:chapterIds)",
    )
    suspend fun getDownloadedChapterIds(
        versionId: String,
        chapterIds: List<Long>,
    ): List<Long>

    @Query("DELETE FROM verse_texts WHERE bibleVersionId = :versionId")
    suspend fun deleteVerseTextsByVersion(versionId: String)
}
