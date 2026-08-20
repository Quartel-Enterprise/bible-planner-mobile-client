package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.VerseRef
import com.quare.bibleplanner.core.verseannotations.domain.repository.SavedVerseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeSavedVerseRepository(
    initialSavedRefs: Set<VerseRef> = emptySet(),
) : SavedVerseRepository {
    val savedRefs = MutableStateFlow(initialSavedRefs)

    override fun observeChapterSavedVerses(chapter: ChapterRef): Flow<Set<Int>> = savedRefs.map { current ->
        current
            .filter { it.chapter == chapter }
            .map { it.verseNumber }
            .toSet()
    }

    override suspend fun areAllSaved(refs: List<VerseRef>): Boolean =
        refs.isNotEmpty() && refs.all { it in savedRefs.value }

    override suspend fun setSaved(
        refs: List<VerseRef>,
        isSaved: Boolean,
    ) {
        savedRefs.value = if (isSaved) savedRefs.value + refs else savedRefs.value - refs.toSet()
    }
}
