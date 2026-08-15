package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.SavedVerseDao
import com.quare.bibleplanner.core.provider.room.entity.SavedVerseEntity
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class SavedVerseRepositoryImpl(
    private val savedVerseDao: SavedVerseDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : SavedVerseRepository {
    override fun observeChapterSavedVerses(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<Set<Int>> = savedVerseDao
        .getChapterSavedVersesFlow(
            bookId = bookId.name,
            chapterNumber = chapterNumber,
        ).map { entities -> entities.map { it.verseNumber }.toSet() }

    override suspend fun areAllSaved(refs: List<VerseRef>): Boolean {
        if (refs.isEmpty()) return false
        val savedRefs = storedStateByRef(refs).filterValues { it }.keys
        return refs.all { ref -> ref in savedRefs }
    }

    override suspend fun setSaved(
        refs: List<VerseRef>,
        isSaved: Boolean,
    ) {
        val currentState = storedStateByRef(refs)
        val changedRefs = refs.filter { ref -> currentState[ref] != isSaved }
        if (changedRefs.isEmpty()) return
        val now = currentTimestampProvider.getCurrentTimestamp()
        savedVerseDao.upsertSavedVerses(
            changedRefs.map { ref ->
                SavedVerseEntity(
                    bookId = ref.bookId.name,
                    chapterNumber = ref.chapterNumber,
                    verseNumber = ref.verseNumber,
                    isSaved = isSaved,
                    updatedAtEpochMillis = now,
                    isPendingSync = true,
                )
            },
        )
    }

    private suspend fun storedStateByRef(refs: List<VerseRef>): Map<VerseRef, Boolean> = refs
        .groupBy { ref -> ref.bookId to ref.chapterNumber }
        .flatMap { (chapter, chapterRefs) ->
            savedVerseDao.getSavedVerses(
                bookId = chapter.first.name,
                chapterNumber = chapter.second,
                verseNumbers = chapterRefs.map { it.verseNumber },
            )
        }.associate { entity ->
            VerseRef(
                bookId = BookId.valueOf(entity.bookId),
                chapterNumber = entity.chapterNumber,
                verseNumber = entity.verseNumber,
            ) to entity.isSaved
        }
}
