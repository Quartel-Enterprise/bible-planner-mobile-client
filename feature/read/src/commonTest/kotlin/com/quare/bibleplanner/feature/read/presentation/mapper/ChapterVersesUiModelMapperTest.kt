package com.quare.bibleplanner.feature.read.presentation.mapper

import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity
import com.quare.bibleplanner.core.provider.room.relation.VerseWithTexts
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChapterVersesUiModelMapperTest {
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
    fun `GIVEN only another version's texts WHEN mapping THEN returns nothing`() {
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
        )

        // When
        val result = mapper.map(versesWithTexts = verses, versionId = ESV, annotations = annotations)

        // Then
        assertEquals(listOf(false, true), result.map { it.isSaved })
        assertEquals(listOf(null, "note-id"), result.map { it.noteId })
    }

    private fun verse(
        number: Int,
        versions: List<String>,
    ): VerseWithTexts = VerseWithTexts(
        verse = VerseEntity(id = number.toLong(), number = number, chapterId = CHAPTER_ID),
        texts = versions.map { version ->
            VerseTextEntity(verseId = number.toLong(), bibleVersionId = version, text = "$version $number")
        },
    )

    private fun prepareScenario() {
        mapper = ChapterVersesUiModelMapper()
    }

    private companion object {
        const val CHAPTER_ID = 1L
        const val ESV = "ESV"
        const val KJV = "KJV"
        val noAnnotations = ChapterAnnotations(
            highlightColorByVerse = emptyMap(),
            savedVerseNumbers = emptySet(),
            noteIdByVerse = emptyMap(),
        )
    }
}
