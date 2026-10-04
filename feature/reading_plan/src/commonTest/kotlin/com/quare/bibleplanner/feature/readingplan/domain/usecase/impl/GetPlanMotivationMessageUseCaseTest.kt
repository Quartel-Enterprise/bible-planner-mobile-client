package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.DaySituation
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Milestone
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.OverallProgress
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Streak
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetPlanMotivationMessageUseCaseTest {
    private val fixedNowMillis = 1_700_000_000_000L
    private val fixedToday = LocalDate(2026, 5, 24)
    private val timestampProvider = CurrentTimestampProvider { fixedNowMillis }
    private val dateProvider = LocalDateTimeProvider { LocalDateTime(fixedToday, LocalTime(0, 0)) }

    private fun useCase(
        milestone: Milestone? = null,
        streak: Streak? = null,
        daySituation: DaySituation? = null,
        progress: OverallProgress = OverallProgress.Zero,
        onMilestoneInvoked: (days: List<DayModel>, nowMillis: Long) -> Unit = { _, _ -> },
        onStreakInvoked: (days: List<DayModel>, today: LocalDate) -> Unit = { _, _ -> },
        onDaySituationInvoked: (days: List<DayModel>, today: LocalDate) -> Unit = { _, _ -> },
        onProgressInvoked: (progress: Float) -> Unit = {},
    ): GetPlanMotivationMessageUseCase = GetPlanMotivationMessageUseCase(
        currentTimestampProvider = timestampProvider,
        localDateTimeProvider = dateProvider,
        resolveMilestoneMotivation = { days, nowMillis ->
            onMilestoneInvoked(days, nowMillis)
            milestone
        },
        resolveStreakMotivation = { days, today ->
            onStreakInvoked(days, today)
            streak
        },
        resolveDaySituationMotivation = { days, today ->
            onDaySituationInvoked(days, today)
            daySituation
        },
        resolveOverallProgressMotivation = { value ->
            onProgressInvoked(value)
            progress
        },
    )

    @Test
    fun `GIVEN the milestone resolver returns a milestone WHEN getting the message THEN returns the milestone`() {
        // Given
        val getPlanMotivationMessage = useCase(
            milestone = Milestone.EnteredNewTestament,
            streak = Streak.Day7,
            daySituation = DaySituation.Completed,
            progress = OverallProgress.Halfway,
        )
        val weeks = listOf(week(day(number = 1)))

        // When
        val result = getPlanMotivationMessage(
            weeks = weeks,
            bibleProgress = 50f,
        )

        // Then
        assertEquals(Milestone.EnteredNewTestament, result)
    }

    @Test
    fun `GIVEN no milestone WHEN getting the message THEN falls back to the streak`() {
        // Given
        val getPlanMotivationMessage = useCase(
            milestone = null,
            streak = Streak.Day7,
            daySituation = DaySituation.Completed,
            progress = OverallProgress.Halfway,
        )
        val weeks = listOf(week(day(number = 1)))

        // When
        val result = getPlanMotivationMessage(
            weeks = weeks,
            bibleProgress = 50f,
        )

        // Then
        assertEquals(Streak.Day7, result)
    }

    @Test
    fun `GIVEN no milestone and no streak WHEN getting the message THEN falls back to the day situation`() {
        // Given
        val getPlanMotivationMessage = useCase(
            milestone = null,
            streak = null,
            daySituation = DaySituation.NotStarted,
            progress = OverallProgress.Halfway,
        )
        val weeks = listOf(week(day(number = 1)))

        // When
        val result = getPlanMotivationMessage(
            weeks = weeks,
            bibleProgress = 50f,
        )

        // Then
        assertEquals(DaySituation.NotStarted, result)
    }

    @Test
    fun `GIVEN no higher-priority message WHEN getting the message THEN falls back to the overall progress`() {
        // Given
        val getPlanMotivationMessage = useCase(
            milestone = null,
            streak = null,
            daySituation = null,
            progress = OverallProgress.Halfway,
        )
        val weeks = listOf(week(day(number = 1)))

        // When
        val result: PlanMotivationMessage = getPlanMotivationMessage(
            weeks = weeks,
            bibleProgress = 50f,
        )

        // Then
        assertEquals(OverallProgress.Halfway, result)
    }

    @Test
    fun `GIVEN a week of two days WHEN getting the message THEN forwards its days and now to the milestone resolver`() {
        // Given
        var capturedDays: List<DayModel>? = null
        var capturedNow: Long? = null
        val d1 = day(number = 1, passages = listOf(passage(BookId.GEN)))
        val d2 = day(number = 2, passages = listOf(passage(BookId.EXO)))
        val weeks = listOf(week(d1, d2))
        val getPlanMotivationMessage = useCase(
            milestone = Milestone.FirstBookCompleted,
            onMilestoneInvoked = { days, now ->
                capturedDays = days
                capturedNow = now
            },
        )

        // When
        getPlanMotivationMessage(
            weeks = weeks,
            bibleProgress = 0f,
        )

        // Then
        assertEquals(listOf(d1, d2), capturedDays)
        assertEquals(fixedNowMillis, capturedNow)
    }

    @Test
    fun `GIVEN a fixed today WHEN getting the message THEN forwards today to the streak and day situation resolvers`() {
        // Given
        var streakToday: LocalDate? = null
        var daySituationToday: LocalDate? = null
        val getPlanMotivationMessage = useCase(
            milestone = null,
            streak = null,
            daySituation = DaySituation.NotStarted,
            onStreakInvoked = { _, today -> streakToday = today },
            onDaySituationInvoked = { _, today -> daySituationToday = today },
        )
        val weeks = listOf(week(day()))

        // When
        getPlanMotivationMessage(
            weeks = weeks,
            bibleProgress = 0f,
        )

        // Then
        assertEquals(fixedToday, streakToday)
        assertEquals(fixedToday, daySituationToday)
    }

    @Test
    fun `GIVEN a bible progress WHEN getting the message THEN forwards it to the progress resolver`() {
        // Given
        var capturedProgress: Float? = null
        val getPlanMotivationMessage = useCase(
            milestone = null,
            streak = null,
            daySituation = null,
            progress = OverallProgress.Halfway,
            onProgressInvoked = { capturedProgress = it },
        )
        val bibleProgress = 42.5f

        // When
        getPlanMotivationMessage(
            weeks = emptyList(),
            bibleProgress = bibleProgress,
        )

        // Then
        assertEquals(42.5f, capturedProgress)
    }

    private fun week(
        vararg days: DayModel,
        number: Int = 1,
    ): WeekPlanModel = WeekPlanModel(number = number, days = days.toList())
}
