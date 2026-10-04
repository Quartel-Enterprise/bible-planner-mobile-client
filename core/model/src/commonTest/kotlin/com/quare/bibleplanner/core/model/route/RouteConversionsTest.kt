package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.contains
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RouteConversionsTest {
    private val dayRoute = DayNavRoute(
        dayNumber = 2,
        weekNumber = 3,
        readingPlanType = "BOOKS",
    )

    @Test
    fun `GIVEN a day route WHEN opening its study THEN keeps the same day and back`() {
        // Given
        val route = dayRoute

        // When
        val studyRoute = route.toDayStudyNavRoute()

        // Then
        assertEquals(
            DayStudyNavRoute(
                dayNumber = 2,
                weekNumber = 3,
                readingPlanType = "BOOKS",
            ),
            studyRoute,
        )
        assertEquals(dayRoute, studyRoute.toDayNavRoute())
    }

    @Test
    fun `GIVEN a reader route WHEN pairing it with its study THEN is a companion of the same chapter`() {
        // Given
        val readRoute = ReadNavRoute(
            bookId = "GEN",
            chapterNumber = 3,
            isChapterRead = true,
            isFromBookDetails = false,
            targetVerseNumbers = listOf(1),
        )

        // When
        val studyRoute = readRoute.toChapterStudyCompanion()

        // Then
        assertEquals(
            ChapterStudyNavRoute(
                bookId = "GEN",
                chapterNumber = 3,
                isCompanion = true,
            ),
            studyRoute,
        )
    }

    @Test
    fun `GIVEN a reading complete route WHEN going to its day THEN points to the same day`() {
        // Given
        val completeRoute = DayReadingCompleteNavRoute(
            dayNumber = 2,
            weekNumber = 3,
            readingPlanType = "BOOKS",
        )

        // When
        val result = completeRoute.toDayNavRoute()

        // Then
        assertEquals(dayRoute, result)
    }

    @Test
    fun `GIVEN a chat opened from a day WHEN going to its day THEN points to that day`() {
        // Given
        val chatRoute = chatRoute(
            dayNumber = 2,
            weekNumber = 3,
            readingPlanType = "BOOKS",
        )

        // When
        val result = chatRoute.toDayNavRoute()

        // Then
        assertEquals(dayRoute, result)
    }

    @Test
    fun `GIVEN a chat missing any day coordinate WHEN going to its day THEN has no day`() {
        // Given
        val routes = listOf(
            chatRoute(
                dayNumber = null,
                weekNumber = 3,
                readingPlanType = "BOOKS",
            ),
            chatRoute(
                dayNumber = 2,
                weekNumber = null,
                readingPlanType = "BOOKS",
            ),
            chatRoute(
                dayNumber = 2,
                weekNumber = 3,
                readingPlanType = null,
            ),
        )

        // When
        val result = routes.map(ChatNavRoute::toDayNavRoute)

        // Then
        assertEquals(listOf(null, null, null), result)
    }

    @Test
    fun `GIVEN the entry sources and reasons WHEN reading their analytics keys THEN uses the lowercase names`() {
        // Given
        val paywallEntrySources = PaywallEntrySource.entries
        val chatEntrySources = ChatEntrySource.entries
        val paywallTeaserReasons = PaywallTeaserReason.entries

        // When
        val keys = paywallEntrySources.map { it.key } +
            chatEntrySources.map { it.key } +
            paywallTeaserReasons.map { it.key }

        // Then
        assertEquals(
            listOf(
                "profile_menu",
                "day_study",
                "day_study_detail",
                "notes_limit",
                "verse_notes_limit",
                "chat",
                "highlight_custom_color",
                "chapter_study",
                "chapter_listening",
                "day_fab",
                "day_study_questions",
                "chapter_study",
                "highlight_custom_color",
                "chapter_study_limit",
                "chapter_listening_limit",
            ),
            keys,
        )
    }

    @Test
    fun `GIVEN the pane metadata WHEN building it THEN flags only its own pane`() {
        // Given
        val keys = listOf(
            DayStudyMainPaneKey,
            DayStudyDetailPaneKey,
            ReaderPaneKey,
            VerseSelectionPaneKey,
            ChapterStudyPaneKey,
            SheetPaneKey,
        )

        // When
        val metadata = listOf(
            getDayStudyMainPane(),
            getDayStudyDetailPane(),
            getReaderPane(),
            getVerseSelectionPane(),
            getChapterStudyPane(),
            getSheetPane(),
        )

        // Then
        assertEquals(
            List(keys.size) { index -> List(keys.size) { it == index } },
            metadata.map { entryMetadata -> keys.map { key -> key in entryMetadata } },
        )
    }

    @Test
    fun `GIVEN a chat opened from a chapter study WHEN going to its day THEN has no day`() {
        // Given
        val chatRoute = ChatNavRoute(
            source = ChatEntrySource.CHAPTER_STUDY,
            dayNumber = null,
            weekNumber = null,
            readingPlanType = null,
            bookId = "GEN",
            chapterNumber = 3,
        )

        // When
        val result = chatRoute.toDayNavRoute()

        // Then
        assertNull(result)
    }

    private fun chatRoute(
        dayNumber: Int?,
        weekNumber: Int?,
        readingPlanType: String?,
    ): ChatNavRoute = ChatNavRoute(
        source = ChatEntrySource.DAY_STUDY_QUESTIONS,
        dayNumber = dayNumber,
        weekNumber = weekNumber,
        readingPlanType = readingPlanType,
        bookId = null,
        chapterNumber = null,
    )
}
