package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.core.verseannotations.domain.usecase.impl.ObserveVersionAnnotationCountsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ObserveVersionAnnotationCountsUseCaseTest {
    private lateinit var versionIds: MutableStateFlow<List<String>>
    private lateinit var passagesByVersion: MutableStateFlow<Map<String, List<AnnotatedPassage>>>

    @Test
    fun `GIVEN no annotated version WHEN observing the counts THEN emits an empty list`() = runTest {
        // Given
        val useCase = prepareScenario(passages = emptyMap())

        // When
        val counts = useCase().first()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = counts,
        )
    }

    @Test
    fun `GIVEN annotated versions WHEN observing the counts THEN counts the passages of each version`() = runTest {
        // Given
        val useCase = prepareScenario(
            passages = mapOf(
                "A21" to listOf(passage("A21")),
                "ACF" to listOf(
                    passage("ACF"),
                    passage(
                        versionId = "ACF",
                        verseNumber = 7,
                    ),
                ),
            ),
        )

        // When
        val counts = useCase().first()

        // Then
        assertEquals(
            expected = listOf(
                VersionAnnotationCount(
                    bibleVersionId = "A21",
                    count = 1,
                ),
                VersionAnnotationCount(
                    bibleVersionId = "ACF",
                    count = 2,
                ),
            ),
            actual = counts,
        )
    }

    @Test
    fun `GIVEN a version without passages WHEN observing the counts THEN leaves it out`() = runTest {
        // Given
        val useCase = prepareScenario(
            passages = mapOf(
                "A21" to listOf(passage("A21")),
                "NVI" to emptyList(),
            ),
        )

        // When
        val counts = useCase().first()

        // Then
        assertEquals(
            expected = listOf("A21"),
            actual = counts.map { it.bibleVersionId },
        )
    }

    @Test
    fun `GIVEN observed counts WHEN a version gains a passage and another version appears THEN updates the counts`() =
        runTest {
            // Given
            val useCase = prepareScenario(passages = mapOf("A21" to listOf(passage("A21"))))
            val emissions = mutableListOf<List<VersionAnnotationCount>>()
            backgroundScope.launch { useCase().toList(emissions) }
            runCurrent()

            // When
            passagesByVersion.value = mapOf(
                "A21" to listOf(
                    passage("A21"),
                    passage(
                        versionId = "A21",
                        verseNumber = 7,
                    ),
                ),
                "NVI" to listOf(passage("NVI")),
            )
            versionIds.value = listOf("A21", "NVI")
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(
                    VersionAnnotationCount(
                        bibleVersionId = "A21",
                        count = 2,
                    ),
                    VersionAnnotationCount(
                        bibleVersionId = "NVI",
                        count = 1,
                    ),
                ),
                actual = emissions.last(),
            )
        }

    private fun passage(
        versionId: String,
        verseNumber: Int = 4,
    ): AnnotatedPassage = AnnotatedPassage(
        chapter = ChapterRef(
            bibleVersionId = versionId,
            bookId = BookId.JOB,
            chapterNumber = 38,
        ),
        verseNumbers = listOf(verseNumber),
        highlightColor = null,
        isSaved = true,
        note = null,
        updatedAtEpochMillis = 0L,
    )

    private fun prepareScenario(passages: Map<String, List<AnnotatedPassage>>): ObserveVersionAnnotationCountsUseCase {
        versionIds = MutableStateFlow(passages.keys.sorted())
        passagesByVersion = MutableStateFlow(passages)
        return ObserveVersionAnnotationCountsUseCase(
            annotatedVersionRepository = { versionIds },
            observeAnnotatedPassages = { versionId ->
                passagesByVersion.map { it[versionId].orEmpty() }
            },
        )
    }
}
