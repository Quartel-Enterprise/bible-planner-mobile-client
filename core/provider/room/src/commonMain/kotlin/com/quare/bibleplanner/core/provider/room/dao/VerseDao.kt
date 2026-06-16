package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.PendingVerseRead
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {
    /**
     * Gets a flow of verses for a specific chapter, ordered by their verse number.
     * Use this for observing changes in the verse content.
     *
     * @param chapterId The unique identifier of the chapter.
     * @return A [Flow] emitting the list of verses for the given chapter.
     */
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    fun getVersesByChapterIdFlow(chapterId: Long): Flow<List<VerseEntity>>

    /**
     * Retrieves the list of verses for a specific chapter, ordered by their verse number.
     *
     * @param chapterId The unique identifier of the chapter.
     * @return The list of verses for the given chapter.
     */
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    suspend fun getVersesByChapterId(chapterId: Long): List<VerseEntity>

    /**
     * Retrieves the list of verses with their texts for a specific chapter.
     */
    @Transaction
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    suspend fun getVersesWithTextsByChapterId(chapterId: Long): List<VerseWithTexts>

    /**
     * Retrieves the list of verses with their texts for a specific chapter by flow.
     */
    @Transaction
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    fun getVersesWithTextsByChapterIdFlow(chapterId: Long): Flow<List<VerseWithTexts>>

    /**
     * Gets a flow of a single verse by its unique identifier.
     *
     * @param verseId The unique identifier of the verse.
     * @return A [Flow] emitting the verse if found, or null otherwise.
     */
    @Query("SELECT * FROM verses WHERE id = :verseId")
    fun getVerseById(verseId: Long): Flow<VerseEntity?>

    /**
     * Retrieves a single verse by its unique identifier.
     *
     * @param verseId The unique identifier of the verse.
     * @return The verse if found, or null otherwise.
     */
    @Query("SELECT * FROM verses WHERE id = :verseId")
    suspend fun getVerseByIdSuspend(verseId: Long): VerseEntity?

    /**
     * Retrieves a specific verse by its chapter ID and verse number.
     *
     * @param chapterId The unique identifier of the chapter.
     * @param verseNumber The number of the verse within the chapter.
     * @return The specific verse matching the criteria, or null if not found.
     */
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId AND number = :verseNumber")
    suspend fun getVerseByChapterIdAndNumber(
        chapterId: Long,
        verseNumber: Int,
    ): VerseEntity?

    /**
     * Inserts or updates a single verse.
     *
     * @param verse The verse entity to be upserted.
     * @return The row ID of the upserted verse.
     */
    @Upsert
    suspend fun upsertVerse(verse: VerseEntity): Long

    /**
     * Inserts or updates a list of verses.
     *
     * @param verses The list of verse entities to be upserted.
     * @return The list of row IDs for the upserted verses.
     */
    @Upsert
    suspend fun upsertVerses(verses: List<VerseEntity>): List<Long>

    /**
     * Inserts or updates a list of verse texts.
     *
     * @param verseTexts The list of verse text entities to be upserted.
     * @return The list of row IDs for the upserted verse texts.
     */
    @Upsert
    suspend fun upsertVerseTexts(verseTexts: List<VerseTextEntity>): List<Long>

    /**
     * Updates the content of an existing verse.
     *
     * @param verse The verse entity with updated data.
     */
    @Update
    suspend fun updateVerse(verse: VerseEntity)

    /**
     * Updates the global read status for a verse identified by its primary verse ID.
     * This performs a lookup to map the verse ID back to the chapter and verse number.
     *
     * @param verseId The unique identifier of the verse.
     * @param isRead The new read status to be applied.
     */
    @Query(
        "UPDATE verses SET isRead = :isRead WHERE chapterId = (SELECT chapterId FROM verses WHERE id = :verseId) AND number = (SELECT number FROM verses WHERE id = :verseId)",
    )
    suspend fun updateVerseReadStatus(
        verseId: Long,
        isRead: Boolean,
    )

    /**
     * Updates the global read status for all verses in a specific chapter.
     *
     * @param chapterId The unique identifier of the chapter.
     * @param isRead The new read status to be applied to all verses in the chapter.
     */
    @Query("UPDATE verses SET isRead = :isRead WHERE chapterId = :chapterId")
    suspend fun updateVersesReadStatusByChapter(
        chapterId: Long,
        isRead: Boolean,
    )

    /**
     * Updates the global read status for all verses belonging to a specific book.
     *
     * @param bookId The unique identifier of the book.
     * @param isRead The new read status to be applied to all verses in the book.
     */
    @Query("UPDATE verses SET isRead = :isRead WHERE chapterId IN (SELECT id FROM chapters WHERE bookId = :bookId)")
    suspend fun updateVersesReadStatusByBook(
        bookId: String,
        isRead: Boolean,
    )

    /**
     * Updates the global read status for a range of verses in a specific chapter.
     *
     * @param chapterId The unique identifier of the chapter.
     * @param startVerse The starting verse number of the range.
     * @param endVerse The ending verse number of the range.
     * @param isRead The new read status to be applied.
     */
    @Query(
        "UPDATE verses SET isRead = :isRead, readUpdatedAt = :updatedAt, isReadPendingSync = 1 " +
            "WHERE chapterId = :chapterId AND number BETWEEN :startVerse AND :endVerse",
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

    /**
     * Cascades a remote whole-chapter read down to its verses so the chapter screen (which derives the
     * checkmark from `verses.all { isRead }`) stays consistent. Skips verses with their own pending
     * verse-range edit so an in-flight local change is not clobbered.
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

    /**
     * Marks pre-sync verse-range reads pending on first launch: only verses read inside a chapter that
     * is not itself fully read, so whole-chapter legacy reads stay at chapter granularity.
     */
    @Query(
        "UPDATE verses SET isReadPendingSync = 1, readUpdatedAt = :now " +
            "WHERE isRead = 1 AND readUpdatedAt IS NULL " +
            "AND chapterId IN (SELECT id FROM chapters WHERE isRead = 0)",
    )
    suspend fun markLegacyVerseReadsPending(now: Long)

    /** Logout wipe: clears verse read state without scheduling a push. */
    @Query("UPDATE verses SET isRead = 0, readUpdatedAt = NULL, isReadPendingSync = 0")
    suspend fun clearAllVerseReadSync()

    /**
     * Delete-progress wipe: clears all verse read state locally and schedules a push for verses that
     * already had a remote row (readUpdatedAt not null), so the deletion propagates to other devices.
     */
    @Query(
        "UPDATE verses SET isRead = 0, " +
            "isReadPendingSync = CASE WHEN readUpdatedAt IS NOT NULL THEN 1 ELSE isReadPendingSync END, " +
            "readUpdatedAt = CASE WHEN readUpdatedAt IS NOT NULL THEN :now ELSE readUpdatedAt END",
    )
    suspend fun resetAllVerseReadsForSync(now: Long)

    /**
     * Deletes a specific verse by its unique identifier.
     *
     * @param verseId The unique identifier of the verse to be deleted.
     */
    @Query("DELETE FROM verses WHERE id = :verseId")
    suspend fun deleteVerse(verseId: Long)

    /**
     * Deletes all verses belonging to a specific chapter.
     *
     * @param chapterId The unique identifier of the chapter whose verses will be deleted.
     */
    @Query("DELETE FROM verses WHERE chapterId = :chapterId")
    suspend fun deleteVersesByChapterId(chapterId: Long)

    /**
     * Counts the number of verses available for a specific chapter and Bible version.
     *
     * @param chapterId The unique identifier of the chapter.
     * @param versionId The abbreviation of the Bible version.
     * @return The count of verses matching the chapter and version.
     */
    @Query(
        "SELECT COUNT(*) FROM verse_texts WHERE bibleVersionId = :versionId AND verseId IN (SELECT id FROM verses WHERE chapterId = :chapterId)",
    )
    suspend fun countVersesByChapterAndVersion(
        chapterId: Long,
        versionId: String,
    ): Int

    /**
     * Counts how many distinct chapters have at least one verse downloaded for a specific version.
     *
     * @param versionId The abbreviation of the Bible version.
     * @return The count of distinct chapters with at least one verse for the version.
     */
    @Query(
        "SELECT COUNT(DISTINCT chapterId) FROM verses INNER JOIN verse_texts ON verses.id = verse_texts.verseId WHERE verse_texts.bibleVersionId = :versionId",
    )
    suspend fun countChaptersWithVersesByVersion(versionId: String): Int

    /**
     * Observes how many distinct chapters have at least one verse downloaded for a specific version.
     *
     * @param versionId The abbreviation of the Bible version.
     * @return A [Flow] emitting the count of distinct chapters with at least one verse for the version.
     */
    @Query(
        "SELECT COUNT(DISTINCT chapterId) FROM verses INNER JOIN verse_texts ON verses.id = verse_texts.verseId WHERE verse_texts.bibleVersionId = :versionId",
    )
    fun countChaptersWithVersesByVersionFlow(versionId: String): Flow<Int>

    /**
     * Observes the downloaded chapter count for every Bible version that has at least one verse.
     *
     * @return A [Flow] emitting a list of [VersionChapterCount], one entry per version.
     */
    @Query(
        "SELECT verse_texts.bibleVersionId, COUNT(DISTINCT verses.chapterId) AS downloadedChapters FROM verse_texts INNER JOIN verses ON verses.id = verse_texts.verseId GROUP BY verse_texts.bibleVersionId",
    )
    fun getDownloadedChaptersPerVersionFlow(): Flow<List<VersionChapterCount>>

    /**
     * Deletes all verse texts for a specific Bible version.
     *
     * @param versionId The abbreviation of the Bible version.
     */
    @Query("DELETE FROM verse_texts WHERE bibleVersionId = :versionId")
    suspend fun deleteVerseTextsByVersion(versionId: String)
}
