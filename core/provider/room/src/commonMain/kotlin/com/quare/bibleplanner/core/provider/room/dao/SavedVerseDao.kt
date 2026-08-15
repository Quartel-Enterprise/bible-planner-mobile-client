package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedVerseDao {
    @Query(
        "SELECT * FROM saved_verses WHERE bookId = :bookId AND chapterNumber = :chapterNumber " +
            "AND isSaved = 1 ORDER BY verseNumber",
    )
    fun getChapterSavedVersesFlow(
        bookId: String,
        chapterNumber: Int,
    ): Flow<List<SavedVerseEntity>>

    @Query(
        "SELECT * FROM saved_verses WHERE bookId = :bookId AND chapterNumber = :chapterNumber " +
            "AND verseNumber IN (:verseNumbers)",
    )
    suspend fun getSavedVerses(
        bookId: String,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): List<SavedVerseEntity>

    @Upsert
    suspend fun upsertSavedVerses(savedVerses: List<SavedVerseEntity>)

    @Query("SELECT * FROM saved_verses WHERE isPendingSync = 1")
    fun getPendingSyncSavedVersesFlow(): Flow<List<SavedVerseEntity>>

    @Query("SELECT * FROM saved_verses WHERE isPendingSync = 1")
    suspend fun getPendingSyncSavedVerses(): List<SavedVerseEntity>

    @Query(
        "UPDATE saved_verses SET isPendingSync = 0 WHERE bookId = :bookId " +
            "AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber " +
            "AND updatedAtEpochMillis = :syncedUpdatedAt",
    )
    suspend fun markSavedVerseSynced(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        syncedUpdatedAt: Long,
    )

    @Transaction
    suspend fun applyRemoteSavedVerse(savedVerse: SavedVerseEntity) {
        insertSavedVerseIfAbsent(savedVerse)
        updateRemoteSavedVerse(
            bookId = savedVerse.bookId,
            chapterNumber = savedVerse.chapterNumber,
            verseNumber = savedVerse.verseNumber,
            isSaved = savedVerse.isSaved,
            remoteUpdatedAt = savedVerse.updatedAtEpochMillis,
        )
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedVerseIfAbsent(savedVerse: SavedVerseEntity)

    @Query(
        "UPDATE saved_verses SET isSaved = :isSaved, updatedAtEpochMillis = :remoteUpdatedAt " +
            "WHERE bookId = :bookId AND chapterNumber = :chapterNumber AND verseNumber = :verseNumber " +
            "AND isPendingSync = 0 AND updatedAtEpochMillis < :remoteUpdatedAt",
    )
    suspend fun updateRemoteSavedVerse(
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        isSaved: Boolean,
        remoteUpdatedAt: Long,
    )

    @Query("DELETE FROM saved_verses")
    suspend fun deleteAllSavedVerses()
}
