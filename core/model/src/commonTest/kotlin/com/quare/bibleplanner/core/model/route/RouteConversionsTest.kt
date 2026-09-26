package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.contains
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RouteConversionsTest {
    private val dayRoute = DayNavRoute(
        dayNumber = 2,
        weekNumber = 3,
        readingPlanType = "BOOKS",
    )

    @Test
    fun `GIVEN a day route WHEN opening its study THEN keeps the same day and back`() {
        // When
        val studyRoute = dayRoute.toDayStudyNavRoute()

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
        // When
        val keys = PaywallEntrySource.entries.map { it.key } +
            ChatEntrySource.entries.map { it.key } +
            PaywallTeaserReason.entries.map { it.key }

        // Then
        assertEquals(
            listOf(
                "profile_menu",
                "day_study",
                "day_study_detail",
                "notes_limit",
                "chat",
                "highlight_custom_color",
                "day_fab",
                "day_study_questions",
                "highlight_custom_color",
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
        )

        // When
        val metadata = listOf(
            getDayStudyMainPane(),
            getDayStudyDetailPane(),
            getReaderPane(),
            getVerseSelectionPane(),
        )

        // Then
        assertEquals(
            List(keys.size) { index -> List(keys.size) { it == index } },
            metadata.map { entryMetadata -> keys.map { key -> key in entryMetadata } },
        )
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
    )
}
