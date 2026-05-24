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
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveDaySituationMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveMilestoneMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveOverallProgressMotivation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveStreakMotivation
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
    ) = GetPlanMotivationMessageUseCase(
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
    fun `returns milestone when milestone resolver returns non-null`() {
        val result = useCase(
            milestone = Milestone.EnteredNewTestament,
            streak = Streak.Day7,
            daySituation = DaySituation.Completed,
            progress = OverallProgress.Halfway,
        ).invoke(weeks = listOf(week(day(number = 1))), bibleProgress = 50f)

        assertEquals(Milestone.EnteredNewTestament, result)
    }

    @Test
    fun `falls back to streak when milestone is null`() {
        val result = useCase(
            milestone = null,
            streak = Streak.Day7,
            daySituation = DaySituation.Completed,
            progress = OverallProgress.Halfway,
        ).invoke(weeks = listOf(week(day(number = 1))), bibleProgress = 50f)

        assertEquals(Streak.Day7, result)
    }

    @Test
    fun `falls back to day situation when milestone and streak are null`() {
        val result = useCase(
            milestone = null,
            streak = null,
            daySituation = DaySituation.NotStarted,
            progress = OverallProgress.Halfway,
        ).invoke(weeks = listOf(week(day(number = 1))), bibleProgress = 50f)

        assertEquals(DaySituation.NotStarted, result)
    }

    @Test
    fun `falls back to overall progress when all higher-priority resolvers return null`() {
        val result: PlanMotivationMessage = useCase(
            milestone = null,
            streak = null,
            daySituation = null,
            progress = OverallProgress.Halfway,
        ).invoke(weeks = listOf(week(day(number = 1))), bibleProgress = 50f)

        assertEquals(OverallProgress.Halfway, result)
    }

    @Test
    fun `forwards flattened days and now to milestone resolver`() {
        var capturedDays: List<DayModel>? = null
        var capturedNow: Long? = null
        val d1 = day(number = 1, passages = listOf(passage(BookId.GEN)))
        val d2 = day(number = 2, passages = listOf(passage(BookId.EXO)))
        val weeks = listOf(week(d1, d2))

        useCase(
            milestone = Milestone.FirstBookCompleted,
            onMilestoneInvoked = { days, now ->
                capturedDays = days
                capturedNow = now
            },
        ).invoke(weeks = weeks, bibleProgress = 0f)

        assertEquals(listOf(d1, d2), capturedDays)
        assertEquals(fixedNowMillis, capturedNow)
    }

    @Test
    fun `forwards today to streak and day situation resolvers`() {
        var streakToday: LocalDate? = null
        var daySituationToday: LocalDate? = null

        useCase(
            milestone = null,
            streak = null,
            daySituation = DaySituation.NotStarted,
            onStreakInvoked = { _, today -> streakToday = today },
            onDaySituationInvoked = { _, today -> daySituationToday = today },
        ).invoke(weeks = listOf(week(day())), bibleProgress = 0f)

        assertEquals(fixedToday, streakToday)
        assertEquals(fixedToday, daySituationToday)
    }

    @Test
    fun `forwards bible progress to progress resolver`() {
        var capturedProgress: Float? = null

        useCase(
            milestone = null,
            streak = null,
            daySituation = null,
            progress = OverallProgress.Halfway,
            onProgressInvoked = { capturedProgress = it },
        ).invoke(weeks = emptyList(), bibleProgress = 42.5f)

        assertEquals(42.5f, capturedProgress)
    }

    private fun week(
        vararg days: DayModel,
        number: Int = 1,
    ): WeekPlanModel = WeekPlanModel(number = number, days = days.toList())
}
