package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.repository.VerseHighlightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeVerseHighlightRepository(
    initialColors: Map<VerseRef, HighlightColor> = emptyMap(),
) : VerseHighlightRepository {
    val colors = MutableStateFlow(initialColors)
    var removedColorKeys: List<String> = emptyList()
        private set

    override fun observeChapterHighlights(
        bookId: BookId,
        chapterNumber: Int,
    ): Flow<Map<Int, HighlightColor>> = colors.map { current ->
        current
            .filterKeys { it.bookId == bookId && it.chapterNumber == chapterNumber }
            .mapKeys { (ref, _) -> ref.verseNumber }
    }

    override suspend fun getColors(refs: List<VerseRef>): Map<VerseRef, HighlightColor?> =
        refs.associateWith { ref -> colors.value[ref] }

    override suspend fun setColor(
        refs: List<VerseRef>,
        color: HighlightColor?,
    ) {
        colors.value = colors.value.toMutableMap().apply {
            refs.forEach { ref ->
                if (color == null) remove(ref) else put(ref, color)
            }
        }
    }

    override suspend fun removeAllWithColor(colorKey: String) {
        removedColorKeys = removedColorKeys + colorKey
        colors.value = colors.value.filterValues { it.key != colorKey }
    }
}
