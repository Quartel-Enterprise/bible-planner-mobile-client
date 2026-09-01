package com.quare.bibleplanner.feature.readingplan.screenshots

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.getGlobalDayIndex
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanDayRef
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMode
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanStatus
import com.quare.bibleplanner.feature.readingplan.presentation.model.DayPlanPresentationModel
import com.quare.bibleplanner.feature.readingplan.presentation.model.ReadingPlanUiState
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekGroup
import com.quare.bibleplanner.feature.readingplan.presentation.model.WeekPlanPresentationModel
import kotlinx.datetime.LocalDate

/**
 * The week the plan screenshot shows: 1 Kings walked three chapters a day, which is roughly where a
 * whole-Bible year plan sits at week 16.
 */
private const val TOTAL_WEEKS = 52
private const val DAYS_PER_WEEK = 7
private const val CURRENT_WEEK = 16
private const val DAYS_READ_THIS_WEEK = 3
private const val TOTAL_DAYS = TOTAL_WEEKS * DAYS_PER_WEEK
private const val READ_DAYS = (CURRENT_WEEK - 1) * DAYS_PER_WEEK + DAYS_READ_THIS_WEEK
private val weekStartDate = LocalDate(year = 2026, monthNumber = 7, dayOfMonth = 20)
private val currentWeekChapters = listOf(
    1 to 3,
    4 to 6,
    7 to 9,
    10 to 12,
    13 to 15,
    16 to 18,
    19 to 22,
)

private fun passage(
    firstChapter: Int,
    lastChapter: Int,
) = PassageModel(
    bookId = BookId.FIRST_KI,
    chapters = (firstChapter..lastChapter).map { chapter ->
        ChapterModel(
            number = chapter,
            startVerse = null,
            endVerse = null,
            bookId = BookId.FIRST_KI,
        )
    },
    isRead = false,
    chapterRanges = "$firstChapter-$lastChapter",
)

private fun currentWeekDays(): List<DayPlanPresentationModel> =
    currentWeekChapters.mapIndexed { index, (firstChapter, lastChapter) ->
        val isRead = index < DAYS_READ_THIS_WEEK
        val isToday = index == DAYS_READ_THIS_WEEK
        val dayInWeek = index + 1
        val globalDayIndex = getGlobalDayIndex(weekNumber = CURRENT_WEEK, dayNumber = dayInWeek)
        DayPlanPresentationModel(
            day = DayModel(
                number = dayInWeek,
                passages = listOf(passage(firstChapter, lastChapter)),
                isRead = isRead,
                totalVerses = 96,
                readVerses = if (isRead) 96 else 0,
                readTimestamp = null,
                plannedReadDate = weekStartDate.plusDays(index),
                notes = null,
                isToday = isToday,
            ),
            globalDayIndex = globalDayIndex,
            shouldShowYear = false,
            isNextToRead = isToday,
            isOverdue = false,
            isActive = isToday,
            isAccented = isToday,
        )
    }

private fun LocalDate.plusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() + days)

/**
 * Weeks other than the current one are collapsed, so their day rows are never composed — only their
 * counts feed the "N of M days" progress card, which is why they carry no [DayPlanPresentationModel].
 */
private fun collapsedWeek(
    number: Int,
    group: WeekGroup,
) = WeekPlanPresentationModel(
    weekPlan = WeekPlanModel(number = number, days = emptyList()),
    dayPlans = emptyList(),
    group = group,
    isExpanded = false,
    readDaysCount = if (group == WeekGroup.Completed) DAYS_PER_WEEK else 0,
    totalDays = DAYS_PER_WEEK,
)

private fun weekPlans(): List<WeekPlanPresentationModel> = (1..TOTAL_WEEKS).map { number ->
    when {
        number < CURRENT_WEEK -> collapsedWeek(number, WeekGroup.Completed)

        number > CURRENT_WEEK -> collapsedWeek(number, WeekGroup.Upcoming)

        else -> {
            val dayPlans = currentWeekDays()
            WeekPlanPresentationModel(
                weekPlan = WeekPlanModel(
                    number = number,
                    days = dayPlans.map { it.day },
                ),
                dayPlans = dayPlans,
                group = WeekGroup.Current,
                isExpanded = true,
                readDaysCount = DAYS_READ_THIS_WEEK,
                totalDays = DAYS_PER_WEEK,
            )
        }
    }
}

private fun planStatus(): PlanStatus {
    val (firstChapter, lastChapter) = currentWeekChapters[DAYS_READ_THIS_WEEK]
    val nextDayInWeek = DAYS_READ_THIS_WEEK + 1
    val nextDay = PlanDayRef(
        weekNumber = CURRENT_WEEK,
        dayNumber = nextDayInWeek,
        globalIndex = getGlobalDayIndex(weekNumber = CURRENT_WEEK, dayNumber = nextDayInWeek),
        passages = listOf(passage(firstChapter, lastChapter)),
        plannedReadDate = weekStartDate.plusDays(DAYS_READ_THIS_WEEK),
    )
    return PlanStatus(
        mode = PlanMode.OnTrack,
        nextDay = nextDay,
        todayDay = nextDay,
        totalDays = TOTAL_DAYS,
        readDays = READ_DAYS,
        streakDays = 14,
        daysAhead = 0,
        daysBehind = 0,
        daysSinceLastRead = 0,
    )
}

internal fun readingPlanUiState(): ReadingPlanUiState.Loaded = ReadingPlanUiState.Loaded(
    weekPlans = weekPlans(),
    progress = READ_DAYS.toFloat() / TOTAL_DAYS * 100,
    motivationMessage = PlanMotivationMessage.Streak.Day14,
    planStatus = planStatus(),
    upcomingExpanded = false,
    completedExpanded = false,
    flashTargetGlobalIndex = -1,
    selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
    isShowingMenu = false,
    isShowingOrderMenu = false,
    scrollToWeekNumber = CURRENT_WEEK,
    scrollToWeekIsAutomatic = false,
    scrollToTop = false,
    isScrolledDown = false,
    isActiveRowVisible = true,
)
