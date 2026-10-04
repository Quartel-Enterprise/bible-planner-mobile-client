package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkPosition
import com.quare.bibleplanner.feature.read.presentation.model.VerseNoteMarkUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChapterVersesUiModelMapperTest {
    private val noAnnotations = ChapterAnnotations(
        highlightColorByVerse = emptyMap(),
        savedVerseNumbers = emptySet(),
        noteIdByVerse = emptyMap(),
        noteVerseNumbersById = emptyMap(),
    )
    private lateinit var mapper: ChapterVersesUiModelMapper

    @Test
    fun `GIVEN text for every verse WHEN mapping THEN returns every verse in order`() {
        // Given
        prepareScenario()
        val verses = listOf(verse(number = 1, versions = listOf(ESV)), verse(number = 2, versions = listOf(ESV)))

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = noAnnotations)

        // Then
        assertEquals(listOf(1, 2), result.map { it.number })
        assertEquals("ESV 2", result.last().text)
    }

    @Test
    fun `GIVEN a verse the version leaves out WHEN mapping THEN skips only that verse`() {
        // Given
        prepareScenario()
        val verses = listOf(
            verse(number = 20, versions = listOf(ESV, KJV)),
            verse(number = 21, versions = listOf(KJV)),
            verse(number = 22, versions = listOf(ESV, KJV)),
        )

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = noAnnotations)

        // Then
        assertEquals(listOf(20, 22), result.map { it.number })
    }

    @Test
    fun `GIVEN only the texts of another version WHEN mapping THEN returns nothing`() {
        // Given
        prepareScenario()
        val verses = listOf(verse(number = 1, versions = listOf(KJV)))

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = noAnnotations)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `GIVEN annotations WHEN mapping THEN decorates the verse they belong to`() {
        // Given
        prepareScenario()
        val verses = listOf(verse(number = 1, versions = listOf(ESV)), verse(number = 2, versions = listOf(ESV)))
        val annotations = ChapterAnnotations(
            highlightColorByVerse = emptyMap(),
            savedVerseNumbers = setOf(2),
            noteIdByVerse = mapOf(2 to "note-id"),
            noteVerseNumbersById = mapOf("note-id" to listOf(2)),
        )

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = annotations)

        // Then
        assertEquals(listOf(false, true), result.map { it.isSaved })
        assertEquals(
            expected = listOf(
                null,
                VerseNoteMarkUiModel(
                    noteId = "note-id",
                    noteVerseNumbers = listOf(2),
                    position = VerseNoteMarkPosition.SINGLE,
                ),
            ),
            actual = result.map { it.noteMark },
        )
    }

    @Test
    fun `GIVEN a note over several verses WHEN mapping THEN places each verse along the note`() {
        // Given
        prepareScenario()
        val verses = (1..6).map { number -> verse(number = number, versions = listOf(ESV)) }
        val annotations = noAnnotations.copy(
            noteIdByVerse = mapOf(
                2 to "long-note",
                3 to "long-note",
                4 to "long-note",
                6 to "long-note",
            ),
            noteVerseNumbersById = mapOf("long-note" to listOf(2, 3, 4, 6)),
        )

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = annotations)

        // Then
        assertEquals(
            expected = listOf(
                null,
                VerseNoteMarkPosition.FIRST,
                VerseNoteMarkPosition.MIDDLE,
                VerseNoteMarkPosition.MIDDLE,
                null,
                VerseNoteMarkPosition.LAST,
            ),
            actual = result.map { it.noteMark?.position },
        )
        assertEquals(
            expected = listOf(2, 3, 4, 6),
            actual = result[1].noteMark?.noteVerseNumbers,
        )
    }

    @Test
    fun `GIVEN two notes sharing a verse WHEN mapping THEN each mark keeps the whole passage of its note`() {
        // Given
        prepareScenario()
        val verses = (1..2).map { number -> verse(number = number, versions = listOf(ESV)) }
        val annotations = noAnnotations.copy(
            noteIdByVerse = mapOf(
                1 to "short-note",
                2 to "long-note",
            ),
            noteVerseNumbersById = mapOf(
                "long-note" to listOf(1, 2),
                "short-note" to listOf(1),
            ),
        )

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = annotations)

        // Then
        assertEquals(
            expected = VerseNoteMarkUiModel(
                noteId = "long-note",
                noteVerseNumbers = listOf(1, 2),
                position = VerseNoteMarkPosition.LAST,
            ),
            actual = result[1].noteMark,
        )
    }

    private fun verse(
        number: Int,
        versions: List<String>,
    ): VerseWithTexts = VerseWithTexts(
        verse = VerseEntity(
            id = number.toLong(),
            number = number,
            chapterId = CHAPTER_ID,
            isRead = false,
            readUpdatedAt = null,
            isReadPendingSync = false,
        ),
        texts = versions.map { version ->
            VerseTextEntity(
                verseId = number.toLong(),
                bibleVersionId = version,
                text = "$version $number",
                id = 0,
                heading = null,
            )
        },
    )

    private fun prepareScenario() {
        mapper = ChapterVersesUiModelMapper()
    }

    private companion object {
        const val CHAPTER_ID = 1L
        const val ESV = "ESV"
        const val KJV = "KJV"
    }
}
