package com.quare.bibleplanner.core.provider.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    fun getVersesByChapterIdFlow(chapterId: Long): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE chapterId = :chapterId ORDER BY number")
    suspend fun getVersesByChapterId(chapterId: Long): List<VerseEntity>

    @Query("SELECT * FROM verses WHERE id = :verseId")
    fun getVerseById(verseId: Long): Flow<VerseEntity?>

    @Query("SELECT * FROM verses WHERE id = :verseId")
    suspend fun getVerseByIdSuspend(verseId: Long): VerseEntity?

    @Query("SELECT * FROM verses WHERE chapterId = :chapterId AND number = :verseNumber")
    suspend fun getVerseByChapterIdAndNumber(
        chapterId: Long,
        verseNumber: Int,
    ): VerseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(verse: VerseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>): List<Long>

    @Update
    suspend fun updateVerse(verse: VerseEntity)

    @Query("UPDATE verses SET isRead = :isRead WHERE id = :verseId")
    suspend fun updateVerseReadStatus(
        verseId: Long,
        isRead: Boolean,
    )

    @Query("UPDATE verses SET isRead = :isRead WHERE chapterId = :chapterId")
    suspend fun updateVersesReadStatusByChapter(
        chapterId: Long,
        isRead: Boolean,
    )

    @Query("DELETE FROM verses WHERE id = :verseId")
    suspend fun deleteVerse(verseId: Long)

    @Query("DELETE FROM verses WHERE chapterId = :chapterId")
    suspend fun deleteVersesByChapterId(chapterId: Long)

    @Query("DELETE FROM verses")
    suspend fun deleteAllVerses()
}
