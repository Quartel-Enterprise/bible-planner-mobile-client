package com.quare.bibleplanner.feature.day.presentation.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DeleteRouteNotesMapperTest {
    private lateinit var mapper: DeleteRouteNotesMapper

    @BeforeTest
    fun setUp() {
        mapper = DeleteRouteNotesMapper()
    }

    @Test
    fun `GIVEN a day with notes WHEN mapping THEN returns the delete notes route for that day`() {
        // When
        val route = mapper.map(
            hasNotes = true,
            readingPlanType = ReadingPlanType.BOOKS,
            weekNumber = 3,
            dayNumber = 5,
        )

        // Then
        assertEquals(
            DeleteNotesRoute(
                readingPlanType = "BOOKS",
                week = 3,
                day = 5,
            ),
            route,
        )
    }

    @Test
    fun `GIVEN a day without notes WHEN mapping THEN returns no route`() {
        // When
        val route = mapper.map(
            hasNotes = false,
            readingPlanType = ReadingPlanType.BOOKS,
            weekNumber = 3,
            dayNumber = 5,
        )

        // Then
        assertNull(route)
    }
}
