package com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterVerseTexts
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.books.domain.usecase.GetVerseReference
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.usecase.ObserveAnnotatedPassages
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveAnnotationEntries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
internal class ObserveAnnotationEntriesUseCase(
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    private val observeAnnotatedPassages: ObserveAnnotatedPassages,
    private val getChapterVerseTexts: GetChapterVerseTexts,
    private val getVerseReference: GetVerseReference,
) : ObserveAnnotationEntries {
    override fun invoke(): Flow<List<AnnotationEntry>> = getSelectedVersionIdFlow()
        .flatMapLatest(observeAnnotatedPassages::invoke)
        .mapLatest(::toEntries)

    private suspend fun toEntries(passages: List<AnnotatedPassage>): List<AnnotationEntry> {
        val textsByChapter = passages
            .map { it.chapter }
            .distinct()
            .associateWith { chapter -> getChapterVerseTexts(chapter) }
        return passages.map { passage ->
            val chapterTexts = textsByChapter[passage.chapter].orEmpty()
            AnnotationEntry(
                passage = passage,
                text = passage.verseNumbers
                    .mapNotNull(chapterTexts::get)
                    .joinToString(separator = " "),
                reference = getVerseReference(
                    bookId = passage.chapter.bookId,
                    chapterNumber = passage.chapter.chapterNumber,
                    verseNumbers = passage.verseNumbers,
                ),
            )
        }
    }
}
