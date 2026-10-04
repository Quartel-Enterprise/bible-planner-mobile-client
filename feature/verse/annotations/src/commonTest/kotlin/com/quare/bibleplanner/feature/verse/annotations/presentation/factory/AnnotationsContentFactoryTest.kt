package com.quare.bibleplanner.feature.verse.annotations.presentation.factory

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.PresetHighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.model.VersionAnnotationCount
import com.quare.bibleplanner.feature.verse.annotations.domain.model.AnnotationEntry
import com.quare.bibleplanner.feature.verse.annotations.fixture.daysAgo
import com.quare.bibleplanner.feature.verse.annotations.fixture.sampleNow
import com.quare.bibleplanner.feature.verse.annotations.fixture.samplePassage
import com.quare.bibleplanner.feature.verse.annotations.fixture.toEntry
import com.quare.bibleplanner.feature.verse.annotations.fixture.utcLocalDateTimeProvider
import com.quare.bibleplanner.feature.verse.annotations.fixture.yellow
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationGroupLabel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsFilters
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.OtherVersionAnnotationsUiModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AnnotationsContentFactoryTest {
    private val green = HighlightColor.Preset(PresetHighlightColor.GREEN)
    private val factory = AnnotationsContentFactory(
        currentTimestampProvider = { sampleNow },
        localDateTimeProvider = utcLocalDateTimeProvider,
    )
    private val highlighted = samplePassage(
        bookId = BookId.JHN,
        chapterNumber = 3,
        verseNumbers = listOf(16),
        highlightColor = yellow,
    ).toEntry()
    private val saved = samplePassage(
        bookId = BookId.PSA,
        chapterNumber = 23,
        isSaved = true,
        updatedAt = daysAgo(1),
    ).toEntry()
    private val noted = samplePassage(
        bookId = BookId.GEN,
        chapterNumber = 3,
        verseNumbers = listOf(15),
        highlightColor = green,
        noteText = "Protoevangelium",
        updatedAt = daysAgo(10),
    ).toEntry()
    private val lastYear = samplePassage(
        bookId = BookId.ROM,
        chapterNumber = 8,
        verseNumbers = listOf(28),
        highlightColor = yellow,
        updatedAt = daysAgo(300),
    ).toEntry()
    private val entries = listOf(highlighted, saved, noted, lastYear)

    @Test
    fun `GIVEN marks in other versions WHEN creating the content THEN offers them from the most marked`() {
        // Given
        val otherVersionCounts = listOf(
            VersionAnnotationCount(
                bibleVersionId = "a21",
                count = 1,
            ),
            VersionAnnotationCount(
                bibleVersionId = "nvi",
                count = 2,
            ),
        )

        // When
        val content = factory.create(
            entries = emptyList(),
            otherVersionCounts = otherVersionCounts,
            filters = AnnotationsContentFactory.noFilters,
        )

        // Then
        assertEquals(
            expected = listOf(
                OtherVersionAnnotationsUiModel(
                    bibleVersionId = "nvi",
                    versionAbbreviation = "NVI",
                    count = 2,
                ),
                OtherVersionAnnotationsUiModel(
                    bibleVersionId = "a21",
                    versionAbbreviation = "A21",
                    count = 1,
                ),
            ),
            actual = content.otherVersions,
        )
        assertEquals(
            expected = 0,
            actual = content.totalCount,
        )
    }

    @Test
    fun `GIVEN passages of different dates WHEN creating the content THEN groups them by day and month`() {
        // Given
        val filters = AnnotationsContentFactory.noFilters

        // When
        val content = factory.create(
            entries = entries,
            otherVersionCounts = emptyList(),
            filters = filters,
        )

        // Then
        assertEquals(
            expected = listOf(
                AnnotationGroupLabel.Today to 1,
                AnnotationGroupLabel.Yesterday to 1,
                AnnotationGroupLabel.MonthOfYear(
                    month = Month.SEPTEMBER,
                    year = null,
                ) to 1,
                AnnotationGroupLabel.MonthOfYear(
                    month = Month.NOVEMBER,
                    year = 2025,
                ) to 1,
            ),
            actual = content.groups.map { it.label to it.items.size },
        )
        assertEquals(
            expected = 4,
            actual = content.totalCount,
        )
        assertEquals(
            expected = "ARC",
            actual = content.groups
                .first()
                .items
                .first()
                .versionAbbreviation,
        )
        assertFalse(content.hasActiveFilters)
    }

    @Test
    fun `GIVEN a type filter WHEN creating the content THEN counts every type and keeps only the selected one`() {
        // Given
        val filters = AnnotationsContentFactory.noFilters.copy(type = AnnotationTypeFilter.NOTES)

        // When
        val content = factory.create(
            entries = entries,
            otherVersionCounts = emptyList(),
            filters = filters,
        )

        // Then
        assertEquals(
            expected = listOf(
                AnnotationTypeFilter.ALL to 4,
                AnnotationTypeFilter.HIGHLIGHTS to 3,
                AnnotationTypeFilter.SAVED to 1,
                AnnotationTypeFilter.NOTES to 1,
            ),
            actual = content.typeFilters.map { it.type to it.count },
        )
        assertEquals(
            expected = listOf(noted.passage),
            actual = content.shownPassages(),
        )
        assertTrue(content.hasActiveFilters)
    }

    @Test
    fun `GIVEN a colour and a period filter WHEN creating the content THEN counts colours within the other filters`() {
        // Given
        val custom = HighlightColor.Custom(
            hue = 200,
            lightness = 50,
        )
        val customEntry = samplePassage(
            bookId = BookId.ISA,
            chapterNumber = 41,
            highlightColor = custom,
        ).toEntry()

        // When
        val content = factory.create(
            entries = entries + customEntry,
            otherVersionCounts = emptyList(),
            filters = AnnotationsContentFactory.noFilters.copy(
                color = yellow,
                period = AnnotationPeriod.LAST_30_DAYS,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(highlighted.passage),
            actual = content.shownPassages(),
        )
        val countByColorKey = content.colorFilters.associate { it.color.key to it.count }
        assertEquals(
            expected = 1,
            actual = countByColorKey[yellow.key],
        )
        assertEquals(
            expected = 1,
            actual = countByColorKey[custom.key],
        )
        assertEquals(
            expected = listOf(yellow.key),
            actual = content.colorFilters.filter { it.isSelected }.map { it.color.key },
        )
    }

    @Test
    fun `GIVEN a book filter WHEN creating the content THEN lists the annotated books in canonical order after all`() {
        // Given
        val filters = AnnotationsContentFactory.noFilters.copy(bookId = BookId.PSA)

        // When
        val content = factory.create(
            entries = entries,
            otherVersionCounts = emptyList(),
            filters = filters,
        )

        // Then
        assertEquals(
            expected = listOf(null, BookId.GEN, BookId.PSA, BookId.JHN, BookId.ROM),
            actual = content.bookFilters.map { it.bookId },
        )
        assertEquals(
            expected = listOf(4, 1, 1, 1, 1),
            actual = content.bookFilters.map { it.count },
        )
        assertEquals(
            expected = listOf(saved.passage),
            actual = content.shownPassages(),
        )
        assertEquals(
            expected = BookId.PSA,
            actual = content.selectedBookId,
        )
    }

    @Test
    fun `GIVEN a period filter WHEN creating the content THEN counts and keeps what was marked within it`() {
        // Given
        val filters = AnnotationsContentFactory.noFilters.copy(period = AnnotationPeriod.LAST_7_DAYS)

        // When
        val content = factory.create(
            entries = entries,
            otherVersionCounts = emptyList(),
            filters = filters,
        )

        // Then
        assertEquals(
            expected = listOf(
                AnnotationPeriod.ANY to 4,
                AnnotationPeriod.TODAY to 1,
                AnnotationPeriod.LAST_7_DAYS to 2,
                AnnotationPeriod.LAST_30_DAYS to 3,
                AnnotationPeriod.CUSTOM to 4,
            ),
            actual = content.periodFilters.map { it.period to it.count },
        )
        assertEquals(
            expected = listOf(highlighted.passage, saved.passage),
            actual = content.shownPassages(),
        )
        assertEquals(
            expected = 2,
            actual = content.shownCount,
        )
    }

    @Test
    fun `GIVEN a custom range WHEN creating the content THEN keeps what was marked within it with both ends`() {
        // Given
        val filters = AnnotationsContentFactory.noFilters.copy(
            period = AnnotationPeriod.CUSTOM,
            customRange = AnnotationDateRange(
                start = LocalDate(
                    year = 2026,
                    month = 9,
                    day = 16,
                ),
                end = LocalDate(
                    year = 2026,
                    month = 9,
                    day = 25,
                ),
            ),
        )

        // When
        val content = factory.create(
            entries = entries,
            otherVersionCounts = emptyList(),
            filters = filters,
        )

        // Then
        assertEquals(
            expected = listOf(saved.passage, noted.passage),
            actual = content.shownPassages(),
        )
        assertEquals(
            expected = 2,
            actual = content.periodFilters.single { it.period == AnnotationPeriod.CUSTOM }.count,
        )
        assertEquals(
            expected = LocalDate(
                year = 2026,
                month = 9,
                day = 26,
            ),
            actual = content.today,
        )
    }

    @Test
    fun `GIVEN colours used by different counts WHEN creating the content THEN orders them by use then palette`() {
        // Given
        val pink = HighlightColor.Preset(PresetHighlightColor.PINK)
        val pinkEntries = listOf(1, 2).map { verseNumber ->
            samplePassage(
                bookId = BookId.MAT,
                chapterNumber = 5,
                verseNumbers = listOf(verseNumber * 10),
                highlightColor = pink,
            ).toEntry()
        }

        // When
        val content = factory.create(
            entries = entries + pinkEntries,
            otherVersionCounts = emptyList(),
            filters = AnnotationsContentFactory.noFilters,
        )

        // Then
        assertEquals(
            expected = listOf(
                PresetHighlightColor.YELLOW to 2,
                PresetHighlightColor.PINK to 2,
                PresetHighlightColor.GREEN to 1,
                PresetHighlightColor.TEAL to 0,
                PresetHighlightColor.ORANGE to 0,
                PresetHighlightColor.PURPLE to 0,
            ),
            actual = content.colorFilters.map { (it.color as HighlightColor.Preset).preset to it.count },
        )
    }

    @Test
    fun `GIVEN a search query WHEN searching THEN matches reference text and note ignoring accents and case`() {
        // Given
        val reference = highlighted.copy(reference = "João 3:16")
        val text = saved.copy(text = "O Senhor é o meu pastor")
        val note = noted.copy(
            passage = noted.passage.copy(
                note = noted.passage.note?.copy(text = "Promessa do Messias"),
            ),
        )
        val search = { query: String ->
            factory
                .create(
                    entries = listOf(reference, text, note, lastYear),
                    otherVersionCounts = emptyList(),
                    filters = AnnotationsContentFactory.noFilters.copy(query = query),
                ).shownPassages()
        }

        // When
        val byReference = search("  JOAO 3:16 ")
        val byText = search("pastor")
        val byNote = search("messías")
        val byNothing = search("xyz")

        // Then
        assertEquals(
            expected = listOf(reference.passage),
            actual = byReference,
        )
        assertEquals(
            expected = listOf(text.passage),
            actual = byText,
        )
        assertEquals(
            expected = listOf(note.passage),
            actual = byNote,
        )
        assertEquals(
            expected = emptyList(),
            actual = byNothing,
        )
    }

    @Test
    fun `GIVEN a search and a type filter WHEN creating the content THEN combines them`() {
        // Given
        val searchableEntries = listOf(
            highlighted.copy(text = "light"),
            noted.copy(text = "light"),
        )
        val filters = AnnotationsContentFactory.noFilters.copy(
            query = "light",
            type = AnnotationTypeFilter.NOTES,
        )

        // When
        val content = factory.create(
            entries = searchableEntries,
            otherVersionCounts = emptyList(),
            filters = filters,
        )

        // Then
        assertEquals(
            expected = listOf(noted.passage),
            actual = content.shownPassages(),
        )
        assertTrue(content.hasActiveFilters)
    }

    @Test
    fun `GIVEN two passages on the same verses WHEN creating the content THEN gives each a distinct key`() {
        // Given
        val sameVersesWithNote: AnnotationEntry = samplePassage(
            bookId = BookId.JHN,
            chapterNumber = 3,
            verseNumbers = listOf(16),
            noteText = "God so loved",
        ).toEntry()

        // When
        val content = factory.create(
            entries = listOf(highlighted, sameVersesWithNote),
            otherVersionCounts = emptyList(),
            filters = AnnotationsFilters(
                type = AnnotationTypeFilter.ALL,
                color = null,
                bookId = null,
                period = AnnotationPeriod.ANY,
                customRange = null,
                query = "",
            ),
        )

        // Then
        val keys = content.groups.flatMap { group -> group.items.map { it.key } }
        assertEquals(
            expected = keys.distinct(),
            actual = keys,
        )
    }

    private fun AnnotationsContentUiModel.shownPassages(): List<AnnotatedPassage> =
        groups.flatMap { group -> group.items.map { it.passage } }
}
