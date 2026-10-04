package com.quare.bibleplanner.feature.verse.annotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.impl.ObserveAnnotationEntriesUseCase
import com.quare.bibleplanner.feature.verse.annotations.fixture.samplePassage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveAnnotationEntriesUseCaseTest {
    @Test
    fun `GIVEN an annotated passage WHEN observing the entries THEN joins its chapter texts in the selected version`() =
        runTest {
            // Given
            val passage = samplePassage(
                bookId = BookId.PRO,
                chapterNumber = 3,
                verseNumbers = listOf(5, 6, 7),
            )
            val requestedChapters = mutableListOf<ChapterRef>()
            val useCase = ObserveAnnotationEntriesUseCase(
                getSelectedVersionIdFlow = { flowOf(passage.chapter.bibleVersionId) },
                observeAnnotatedPassages = { versionId ->
                    flowOf(listOf(passage).filter { it.chapter.bibleVersionId == versionId })
                },
                getChapterVerseTexts = { chapter ->
                    requestedChapters += chapter
                    mapOf(
                        5 to "Trust in the Lord",
                        6 to "In all your ways",
                    )
                },
                getVerseReference = { bookId, chapterNumber, verseNumbers ->
                    "${bookId.name} $chapterNumber:${verseNumbers.joinToString(separator = "-")}"
                },
            )

            // When
            val entries = useCase().first()

            // Then
            assertEquals(
                expected = listOf(
                    AnnotationEntry(
                        passage = passage,
                        text = "Trust in the Lord In all your ways",
                        reference = "PRO 3:5-6-7",
                    ),
                ),
                actual = entries,
            )
            assertEquals(
                expected = listOf(passage.chapter),
                actual = requestedChapters,
            )
        }

    @Test
    fun `GIVEN a changed selected version WHEN observing the entries THEN follows the selected version`() = runTest {
        // Given
        val selectedVersion = MutableStateFlow("arc")
        val useCase = ObserveAnnotationEntriesUseCase(
            getSelectedVersionIdFlow = { selectedVersion },
            observeAnnotatedPassages = { versionId ->
                flowOf(if (versionId == "web") listOf(samplePassage()) else emptyList())
            },
            getChapterVerseTexts = { emptyMap() },
            getVerseReference = { _, _, _ -> "" },
        )

        // When
        selectedVersion.value = "web"
        val entries = useCase().first()

        // Then
        assertEquals(
            expected = listOf(""),
            actual = entries.map { it.text },
        )
    }
}
