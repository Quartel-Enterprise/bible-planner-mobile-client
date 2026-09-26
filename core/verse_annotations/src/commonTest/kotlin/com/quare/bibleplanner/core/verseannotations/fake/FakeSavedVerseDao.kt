package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.provider.room.dao.SavedVerseDao
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeSavedVerseDao(
    initialRows: List<SavedVerseEntity> = emptyList(),
) : SavedVerseDao {
    val rows = MutableStateFlow(initialRows)
    var upsertCalls: Int = 0
        private set

    override fun getChapterSavedVersesFlow(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
    ): Flow<List<SavedVerseEntity>> = rows.map { current ->
        current
            .filter { it.bibleVersionId == bibleVersionId && it.bookId == bookId && it.chapterNumber == chapterNumber }
            .filter { it.isSaved }
            .sortedBy { it.verseNumber }
    }

    override suspend fun getSavedVerses(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): List<SavedVerseEntity> = rows.value.filter {
        it.bibleVersionId == bibleVersionId &&
            it.bookId == bookId &&
            it.chapterNumber == chapterNumber &&
            it.verseNumber in verseNumbers
    }

    override suspend fun upsertSavedVerses(savedVerses: List<SavedVerseEntity>) {
        upsertCalls++
        rows.value = rows.value.filterNot { row -> savedVerses.any { it.sameKey(row) } } + savedVerses
    }

    override fun getPendingSyncSavedVersesFlow(): Flow<List<SavedVerseEntity>> =
        rows.map { current -> current.filter { it.isPendingSync } }

    override suspend fun getPendingSyncSavedVerses(): List<SavedVerseEntity> = rows.value.filter { it.isPendingSync }

    override suspend fun markSavedVerseSynced(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        syncedUpdatedAt: Long,
    ) {
        rows.value = rows.value.map { row ->
            val isTarget = row.bibleVersionId == bibleVersionId &&
                row.bookId == bookId &&
                row.chapterNumber == chapterNumber &&
                row.verseNumber == verseNumber &&
                row.updatedAtEpochMillis == syncedUpdatedAt
            if (isTarget) row.copy(isPendingSync = false) else row
        }
    }

    override suspend fun insertSavedVerseIfAbsent(savedVerse: SavedVerseEntity) {
        if (rows.value.none { it.sameKey(savedVerse) }) rows.value = rows.value + savedVerse
    }

    override suspend fun updateRemoteSavedVerse(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        isSaved: Boolean,
        remoteUpdatedAt: Long,
    ) {
        rows.value = rows.value.map { row ->
            val isTarget = row.bibleVersionId == bibleVersionId &&
                row.bookId == bookId &&
                row.chapterNumber == chapterNumber &&
                row.verseNumber == verseNumber &&
                !row.isPendingSync &&
                row.updatedAtEpochMillis < remoteUpdatedAt
            if (isTarget) {
                row.copy(
                    isSaved = isSaved,
                    updatedAtEpochMillis = remoteUpdatedAt,
                )
            } else {
                row
            }
        }
    }

    override suspend fun deleteAllSavedVerses() {
        rows.value = emptyList()
    }

    private fun SavedVerseEntity.sameKey(other: SavedVerseEntity): Boolean = bibleVersionId == other.bibleVersionId &&
        bookId == other.bookId &&
        chapterNumber == other.chapterNumber &&
        verseNumber == other.verseNumber
}
