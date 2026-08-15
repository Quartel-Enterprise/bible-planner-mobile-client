package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.room.dao.VerseHighlightDao
import com.quare.bibleplanner.core.provider.room.entity.VerseHighlightEntity
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class VerseHighlightRepositoryImpl(
    private val verseHighlightDao: VerseHighlightDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : VerseHighlightRepository {
    override fun observeChapterHighlights(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<Map<Int, HighlightColor>> = verseHighlightDao
        .getChapterHighlightsFlow(
            bookId = bookId.name,
            chapterNumber = chapterNumber,
        ).map { entities ->
            entities
                .mapNotNull { entity ->
                    entity.color
                        ?.let(HighlightColor::fromKey)
                        ?.let { color -> entity.verseNumber to color }
                }.toMap()
        }

    override suspend fun getColors(refs: List<VerseRef>): Map<VerseRef, HighlightColor?> {
        val storedColorByRef = refs
            .groupBy { ref -> ref.bookId to ref.chapterNumber }
            .flatMap { (chapter, chapterRefs) ->
                verseHighlightDao.getHighlights(
                    bookId = chapter.first.name,
                    chapterNumber = chapter.second,
                    verseNumbers = chapterRefs.map { it.verseNumber },
                )
            }.associateBy { entity ->
                VerseRef(
                    bookId = BookId.valueOf(entity.bookId),
                    chapterNumber = entity.chapterNumber,
                    verseNumber = entity.verseNumber,
                )
            }
        return refs.associateWith { ref ->
            storedColorByRef[ref]?.color?.let(HighlightColor::fromKey)
        }
    }

    /**
     * Rows already holding the requested colour are left untouched: re-marking them pending would
     * cost a push and a realtime broadcast per device for a value nobody changed.
     */
    override suspend fun setColor(
        refs: List<VerseRef>,
        color: HighlightColor?,
    ) {
        val currentColors = getColors(refs)
        val changedRefs = refs.filter { ref -> currentColors[ref]?.key != color?.key }
        if (changedRefs.isEmpty()) return
        val now = currentTimestampProvider.getCurrentTimestamp()
        verseHighlightDao.upsertHighlights(
            changedRefs.map { ref ->
                VerseHighlightEntity(
                    bookId = ref.bookId.name,
                    chapterNumber = ref.chapterNumber,
                    verseNumber = ref.verseNumber,
                    color = color?.key,
                    updatedAtEpochMillis = now,
                    isPendingSync = true,
                )
            },
        )
    }

    override suspend fun removeAllWithColor(colorKey: String) {
        val now = currentTimestampProvider.getCurrentTimestamp()
        val cleared = verseHighlightDao.getHighlightsByColor(colorKey).map { entity ->
            entity.copy(
                color = null,
                updatedAtEpochMillis = now,
                isPendingSync = true,
            )
        }
        if (cleared.isEmpty()) return
        verseHighlightDao.upsertHighlights(cleared)
    }
}
