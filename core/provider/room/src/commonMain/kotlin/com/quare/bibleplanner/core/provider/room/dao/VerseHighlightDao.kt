package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseHighlightDao {
    @Query(
        "SELECT * FROM verse_highlights WHERE bibleVersionId = :bibleVersionId AND bookId = :bookId " +
            "AND chapterNumber = :chapterNumber AND color IS NOT NULL ORDER BY verseNumber",
    )
    fun getChapterHighlightsFlow(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
    ): Flow<List<VerseHighlightEntity>>

    @Query(
        "SELECT * FROM verse_highlights WHERE bibleVersionId = :bibleVersionId AND bookId = :bookId " +
            "AND chapterNumber = :chapterNumber AND verseNumber IN (:verseNumbers)",
    )
    suspend fun getHighlights(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): List<VerseHighlightEntity>

    @Query("SELECT * FROM verse_highlights WHERE color = :color")
    suspend fun getHighlightsByColor(color: String): List<VerseHighlightEntity>

    @Upsert
    suspend fun upsertHighlights(highlights: List<VerseHighlightEntity>)

    @Query("SELECT * FROM verse_highlights WHERE isPendingSync = 1")
    fun getPendingSyncHighlightsFlow(): Flow<List<VerseHighlightEntity>>

    @Query("SELECT * FROM verse_highlights WHERE isPendingSync = 1")
    suspend fun getPendingSyncHighlights(): List<VerseHighlightEntity>

    @Query(
        "UPDATE verse_highlights SET isPendingSync = 0 WHERE bibleVersionId = :bibleVersionId " +
            "AND bookId = :bookId AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber " +
            "AND updatedAtEpochMillis = :syncedUpdatedAt",
    )
    suspend fun markHighlightSynced(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        syncedUpdatedAt: Long,
    )

    /**
     * Last-Write-Wins: the insert creates the row only when this device has never seen the verse, and
     * the update overwrites an existing row only when it holds no pending local edit and the remote
     * change is strictly newer. Together they make the echo of our own push a no-op.
     */
    @Transaction
    suspend fun applyRemoteHighlight(highlight: VerseHighlightEntity) {
        insertHighlightIfAbsent(highlight)
        updateRemoteHighlight(
            bibleVersionId = highlight.bibleVersionId,
            bookId = highlight.bookId,
            chapterNumber = highlight.chapterNumber,
            verseNumber = highlight.verseNumber,
            color = highlight.color,
            remoteUpdatedAt = highlight.updatedAtEpochMillis,
        )
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHighlightIfAbsent(highlight: VerseHighlightEntity)

    @Query(
        "UPDATE verse_highlights SET color = :color, updatedAtEpochMillis = :remoteUpdatedAt " +
            "WHERE bibleVersionId = :bibleVersionId AND bookId = :bookId " +
            "AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber " +
            "AND isPendingSync = 0 AND updatedAtEpochMillis < :remoteUpdatedAt",
    )
    suspend fun updateRemoteHighlight(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        color: String?,
        remoteUpdatedAt: Long,
    )

    /** Logout wipe: drops the local rows without scheduling a push. */
    @Query("DELETE FROM verse_highlights")
    suspend fun deleteAllHighlights()
}
