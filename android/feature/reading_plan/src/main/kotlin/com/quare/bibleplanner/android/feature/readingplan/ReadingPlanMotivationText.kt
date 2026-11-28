package com.quare.bibleplanner.android.feature.readingplan

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.DaySituation
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Milestone
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.OverallProgress
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Streak

@Composable
internal fun PlanMotivationMessage.resolveText(): String = when (this) {
    is Milestone.OnlyOneBookLeft -> stringResource(
        R.string.reading_plan_motivation_milestone_only_one_left,
        bookId.getBookName(),
    )

    is Milestone.BookCompleted -> stringResource(
        R.string.reading_plan_motivation_milestone_book_completed,
        bookId.getBookName(),
    )

    else -> stringResource(toStringRes())
}

private fun PlanMotivationMessage.toStringRes(): Int = when (this) {
    OverallProgress.Zero -> R.string.reading_plan_motivation_overall_zero
    OverallProgress.EarlyStart -> R.string.reading_plan_motivation_overall_early_start
    OverallProgress.BuildingSolid -> R.string.reading_plan_motivation_overall_building_solid
    OverallProgress.ApproachingThird -> R.string.reading_plan_motivation_overall_approaching_third
    OverallProgress.PastThirty -> R.string.reading_plan_motivation_overall_past_thirty
    OverallProgress.Halfway -> R.string.reading_plan_motivation_overall_halfway
    OverallProgress.MoreThanHalf -> R.string.reading_plan_motivation_overall_more_than_half
    OverallProgress.ThreeQuarters -> R.string.reading_plan_motivation_overall_three_quarters
    OverallProgress.FinalStretch -> R.string.reading_plan_motivation_overall_final_stretch
    OverallProgress.AlmostThere -> R.string.reading_plan_motivation_overall_almost_there
    OverallProgress.Completed -> R.string.reading_plan_motivation_overall_completed
    Streak.Day1 -> R.string.reading_plan_motivation_streak_1
    Streak.Day3 -> R.string.reading_plan_motivation_streak_3
    Streak.Day7 -> R.string.reading_plan_motivation_streak_7
    Streak.Day14 -> R.string.reading_plan_motivation_streak_14
    Streak.Day30 -> R.string.reading_plan_motivation_streak_30
    Streak.Day100 -> R.string.reading_plan_motivation_streak_100
    DaySituation.NotStarted -> R.string.reading_plan_motivation_day_not_started
    DaySituation.Started -> R.string.reading_plan_motivation_day_started
    DaySituation.Completed -> R.string.reading_plan_motivation_day_completed
    DaySituation.OneOverdue -> R.string.reading_plan_motivation_day_one_overdue
    DaySituation.MultipleOverdue -> R.string.reading_plan_motivation_day_multiple_overdue
    Milestone.EnteredNewTestament -> R.string.reading_plan_motivation_milestone_entered_new_testament
    is Milestone.OnlyOneBookLeft -> R.string.reading_plan_motivation_milestone_only_one_left
    Milestone.FirstBookCompleted -> R.string.reading_plan_motivation_milestone_first_book_completed
    is Milestone.BookCompleted -> R.string.reading_plan_motivation_milestone_book_completed
}
