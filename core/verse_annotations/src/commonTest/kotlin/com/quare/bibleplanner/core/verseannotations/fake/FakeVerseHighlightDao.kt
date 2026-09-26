package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.provider.room.dao.VerseHighlightDao
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeVerseHighlightDao(
    initialRows: List<VerseHighlightEntity> = emptyList(),
) : VerseHighlightDao {
    val rows = MutableStateFlow(initialRows)
    var upsertCalls: Int = 0
        private set

    override fun getChapterHighlightsFlow(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
    ): Flow<List<VerseHighlightEntity>> = rows.map { current ->
        current
            .filter { it.bibleVersionId == bibleVersionId && it.bookId == bookId && it.chapterNumber == chapterNumber }
            .filter { it.color != null }
            .sortedBy { it.verseNumber }
    }

    override suspend fun getHighlights(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumbers: List<Int>,
    ): List<VerseHighlightEntity> = rows.value.filter {
        it.bibleVersionId == bibleVersionId &&
            it.bookId == bookId &&
            it.chapterNumber == chapterNumber &&
            it.verseNumber in verseNumbers
    }

    override suspend fun getHighlightsByColor(color: String): List<VerseHighlightEntity> =
        rows.value.filter { it.color == color }

    override suspend fun upsertHighlights(highlights: List<VerseHighlightEntity>) {
        upsertCalls++
        rows.value = rows.value.filterNot { row -> highlights.any { it.sameKey(row) } } + highlights
    }

    override fun getPendingSyncHighlightsFlow(): Flow<List<VerseHighlightEntity>> =
        rows.map { current -> current.filter { it.isPendingSync } }

    override suspend fun getPendingSyncHighlights(): List<VerseHighlightEntity> = rows.value.filter { it.isPendingSync }

    override suspend fun markHighlightSynced(
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

    override suspend fun insertHighlightIfAbsent(highlight: VerseHighlightEntity) {
        if (rows.value.none { it.sameKey(highlight) }) rows.value = rows.value + highlight
    }

    override suspend fun updateRemoteHighlight(
        bibleVersionId: String,
        bookId: String,
        chapterNumber: Int,
        verseNumber: Int,
        color: String?,
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
                    color = color,
                    updatedAtEpochMillis = remoteUpdatedAt,
                )
            } else {
                row
            }
        }
    }

    override suspend fun deleteAllHighlights() {
        rows.value = emptyList()
    }

    private fun VerseHighlightEntity.sameKey(other: VerseHighlightEntity): Boolean =
        bibleVersionId == other.bibleVersionId &&
            bookId == other.bookId &&
            chapterNumber == other.chapterNumber &&
            verseNumber == other.verseNumber
}
