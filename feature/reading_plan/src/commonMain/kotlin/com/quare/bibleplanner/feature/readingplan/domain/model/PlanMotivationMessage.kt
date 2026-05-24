package com.quare.bibleplanner.feature.readingplan.domain.model

import com.quare.bibleplanner.core.model.book.BookId

internal sealed interface PlanMotivationMessage {
    sealed interface OverallProgress : PlanMotivationMessage {
        data object Zero : OverallProgress

        data object EarlyStart : OverallProgress

        data object BuildingSolid : OverallProgress

        data object ApproachingThird : OverallProgress

        data object PastThirty : OverallProgress

        data object Halfway : OverallProgress

        data object MoreThanHalf : OverallProgress

        data object ThreeQuarters : OverallProgress

        data object FinalStretch : OverallProgress

        data object AlmostThere : OverallProgress

        data object Completed : OverallProgress
    }

    sealed interface Streak : PlanMotivationMessage {
        data object Day1 : Streak

        data object Day3 : Streak

        data object Day7 : Streak

        data object Day14 : Streak

        data object Day30 : Streak

        data object Day100 : Streak
    }

    sealed interface DaySituation : PlanMotivationMessage {
        data object NotStarted : DaySituation

        data object Started : DaySituation

        data object Completed : DaySituation

        data object OneOverdue : DaySituation

        data object MultipleOverdue : DaySituation
    }

    sealed interface Milestone : PlanMotivationMessage {
        data object EnteredNewTestament : Milestone

        data class OnlyOneBookLeft(
            val bookId: BookId,
        ) : Milestone

        data object FirstBookCompleted : Milestone

        data class BookCompleted(
            val bookId: BookId,
        ) : Milestone
    }
}
