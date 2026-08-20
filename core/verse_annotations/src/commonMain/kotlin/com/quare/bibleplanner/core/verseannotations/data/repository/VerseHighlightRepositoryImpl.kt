package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
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
    override fun observeChapterHighlights(chapter: ChapterRef): Flow<Map<Int, HighlightColor>> = verseHighlightDao
        .getChapterHighlightsFlow(
            bibleVersionId = chapter.bibleVersionId,
            bookId = chapter.bookId.name,
            chapterNumber = chapter.chapterNumber,
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
            .groupBy { ref -> ref.chapter }
            .flatMap { (chapter, chapterRefs) ->
                verseHighlightDao.getHighlights(
                    bibleVersionId = chapter.bibleVersionId,
                    bookId = chapter.bookId.name,
                    chapterNumber = chapter.chapterNumber,
                    verseNumbers = chapterRefs.map { it.verseNumber },
                )
            }.associateBy { entity -> entity.toVerseRef() }
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
                    bibleVersionId = ref.chapter.bibleVersionId,
                    bookId = ref.chapter.bookId.name,
                    chapterNumber = ref.chapter.chapterNumber,
                    verseNumber = ref.verseNumber,
                    color = color?.key,
                    updatedAtEpochMillis = now,
                    isPendingSync = true,
                )
            },
        )
    }

    /** Colours belong to the palette, which is version-independent, so this clears every version. */
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

    private fun VerseHighlightEntity.toVerseRef(): VerseRef = VerseRef(
        chapter = ChapterRef(
            bibleVersionId = bibleVersionId,
            bookId = BookId.valueOf(bookId),
            chapterNumber = chapterNumber,
        ),
        verseNumber = verseNumber,
    )
}
