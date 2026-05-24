package com.quare.bibleplanner.feature.readingplan.presentation.component

import androidx.compose.runtime.Composable
import bibleplanner.feature.reading_plan.generated.resources.Res
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_day_completed
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_day_multiple_overdue
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_day_not_started
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_day_one_overdue
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_day_started
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_milestone_book_completed
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_milestone_entered_new_testament
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_milestone_first_book_completed
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_milestone_only_one_left
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_almost_there
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_approaching_third
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_building_solid
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_completed
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_early_start
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_final_stretch
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_halfway
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_more_than_half
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_past_thirty
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_three_quarters
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_overall_zero
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_streak_1
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_streak_100
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_streak_14
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_streak_3
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_streak_30
import bibleplanner.feature.reading_plan.generated.resources.plan_motivation_streak_7
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.DaySituation
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Milestone
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.OverallProgress
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Streak
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlanMotivationMessage.resolveText(): String = when (this) {
    OverallProgress.Zero -> stringResource(Res.string.plan_motivation_overall_zero)

    OverallProgress.EarlyStart -> stringResource(Res.string.plan_motivation_overall_early_start)

    OverallProgress.BuildingSolid -> stringResource(Res.string.plan_motivation_overall_building_solid)

    OverallProgress.ApproachingThird -> stringResource(Res.string.plan_motivation_overall_approaching_third)

    OverallProgress.PastThirty -> stringResource(Res.string.plan_motivation_overall_past_thirty)

    OverallProgress.Halfway -> stringResource(Res.string.plan_motivation_overall_halfway)

    OverallProgress.MoreThanHalf -> stringResource(Res.string.plan_motivation_overall_more_than_half)

    OverallProgress.ThreeQuarters -> stringResource(Res.string.plan_motivation_overall_three_quarters)

    OverallProgress.FinalStretch -> stringResource(Res.string.plan_motivation_overall_final_stretch)

    OverallProgress.AlmostThere -> stringResource(Res.string.plan_motivation_overall_almost_there)

    OverallProgress.Completed -> stringResource(Res.string.plan_motivation_overall_completed)

    Streak.Day1 -> stringResource(Res.string.plan_motivation_streak_1)

    Streak.Day3 -> stringResource(Res.string.plan_motivation_streak_3)

    Streak.Day7 -> stringResource(Res.string.plan_motivation_streak_7)

    Streak.Day14 -> stringResource(Res.string.plan_motivation_streak_14)

    Streak.Day30 -> stringResource(Res.string.plan_motivation_streak_30)

    Streak.Day100 -> stringResource(Res.string.plan_motivation_streak_100)

    DaySituation.NotStarted -> stringResource(Res.string.plan_motivation_day_not_started)

    DaySituation.Started -> stringResource(Res.string.plan_motivation_day_started)

    DaySituation.Completed -> stringResource(Res.string.plan_motivation_day_completed)

    DaySituation.OneOverdue -> stringResource(Res.string.plan_motivation_day_one_overdue)

    DaySituation.MultipleOverdue -> stringResource(Res.string.plan_motivation_day_multiple_overdue)

    Milestone.EnteredNewTestament -> stringResource(
        Res.string.plan_motivation_milestone_entered_new_testament,
    )

    is Milestone.OnlyOneBookLeft -> stringResource(
        Res.string.plan_motivation_milestone_only_one_left,
        stringResource(bookId.toBookNameResource()),
    )

    Milestone.FirstBookCompleted -> stringResource(
        Res.string.plan_motivation_milestone_first_book_completed,
    )

    is Milestone.BookCompleted -> stringResource(
        Res.string.plan_motivation_milestone_book_completed,
        stringResource(bookId.toBookNameResource()),
    )
}
