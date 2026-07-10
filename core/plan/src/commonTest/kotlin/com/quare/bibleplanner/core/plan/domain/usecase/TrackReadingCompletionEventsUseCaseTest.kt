package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PlansModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TrackReadingCompletionEventsUseCaseTest {
    private lateinit var useCase: TrackReadingCompletionEventsUseCase
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        trackedEvents = mutableListOf()
        useCase = TrackReadingCompletionEventsUseCase(
            trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
        )
    }

    @Test
    fun `GIVEN a week with one unread day WHEN the update completes it THEN fires week_completed`() {
        // Given
        val before = plans(
            chronological = listOf(week(number = 1, daysRead = listOf(true, false))),
            books = listOf(week(number = 1, daysRead = listOf(false, false))),
        )
        val after = plans(
            chronological = listOf(week(number = 1, daysRead = listOf(true, true))),
            books = listOf(week(number = 1, daysRead = listOf(false, false))),
        )

        // When
        useCase(
            before = before,
            after = after,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            weekNumber = 1,
        )

        // Then
        assertEquals(
            listOf(
                "week_completed" to mapOf<String, Any>(
                    "plan_type" to "chronological",
                    "week_number" to 1,
                ),
                "plan_completed" to mapOf<String, Any>("plan_type" to "chronological"),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN an already complete week WHEN the update runs THEN fires nothing`() {
        // Given
        val complete = plans(
            chronological = listOf(
                week(number = 1, daysRead = listOf(true, true)),
                week(number = 2, daysRead = listOf(false)),
            ),
            books = listOf(week(number = 1, daysRead = listOf(false))),
        )

        // When
        useCase(
            before = complete,
            after = complete,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            weekNumber = 1,
        )

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a week completes but later weeks are unread WHEN updating THEN fires only week_completed`() {
        // Given
        val before = plans(
            chronological = listOf(
                week(number = 1, daysRead = listOf(true, false)),
                week(number = 2, daysRead = listOf(false)),
            ),
            books = listOf(week(number = 1, daysRead = listOf(false))),
        )
        val after = plans(
            chronological = listOf(
                week(number = 1, daysRead = listOf(true, true)),
                week(number = 2, daysRead = listOf(false)),
            ),
            books = listOf(week(number = 1, daysRead = listOf(false))),
        )

        // When
        useCase(
            before = before,
            after = after,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            weekNumber = 1,
        )

        // Then
        assertEquals(
            listOf(
                "week_completed" to mapOf<String, Any>(
                    "plan_type" to "chronological",
                    "week_number" to 1,
                ),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN both plans complete together WHEN the update runs THEN fires plan_completed for each plan type`() {
        // Given
        val before = plans(
            chronological = listOf(week(number = 1, daysRead = listOf(true, false))),
            books = listOf(week(number = 1, daysRead = listOf(false))),
        )
        val after = plans(
            chronological = listOf(week(number = 1, daysRead = listOf(true, true))),
            books = listOf(week(number = 1, daysRead = listOf(true))),
        )

        // When
        useCase(
            before = before,
            after = after,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            weekNumber = 1,
        )

        // Then
        assertEquals(
            listOf(
                "week_completed" to mapOf<String, Any>(
                    "plan_type" to "chronological",
                    "week_number" to 1,
                ),
                "plan_completed" to mapOf<String, Any>("plan_type" to "chronological"),
                "plan_completed" to mapOf<String, Any>("plan_type" to "books"),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN empty plans WHEN the update runs THEN fires nothing`() {
        // Given
        val empty = plans(
            chronological = emptyList(),
            books = emptyList(),
        )

        // When
        useCase(
            before = empty,
            after = empty,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            weekNumber = 1,
        )

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    private fun plans(
        chronological: List<WeekPlanModel>,
        books: List<WeekPlanModel>,
    ): PlansModel = PlansModel(
        chronologicalOrder = chronological,
        booksOrder = books,
    )

    private fun week(
        number: Int,
        daysRead: List<Boolean>,
    ): WeekPlanModel = WeekPlanModel(
        number = number,
        days = daysRead.mapIndexed { index, isRead ->
            DayModel(
                number = index + 1,
                passages = emptyList(),
                isRead = isRead,
                totalVerses = 0,
                readVerses = 0,
                readTimestamp = null,
                plannedReadDate = null,
                notes = null,
                isToday = false,
            )
        },
    )
}
