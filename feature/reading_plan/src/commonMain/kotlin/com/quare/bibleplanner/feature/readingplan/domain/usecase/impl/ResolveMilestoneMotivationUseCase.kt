package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.isNewTestament
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Milestone
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveMilestoneMotivation

private const val MILESTONE_WINDOW_MILLIS: Long = 24L * 60L * 60L * 1000L

internal class ResolveMilestoneMotivationUseCase : ResolveMilestoneMotivation {
    override fun invoke(
        days: List<DayModel>,
        nowMillis: Long,
    ): Milestone? {
        val booksInPlan = days
            .flatMap { it.passages }
            .map { it.bookId }
            .distinct()
        if (booksInPlan.isEmpty()) return null

        val booksWithUnreadPassages = days
            .flatMap { it.passages }
            .filter { !it.isRead }
            .map { it.bookId }
            .distinct()
        val booksFullyRead = booksInPlan - booksWithUnreadPassages.toSet()

        val mostRecentReadDay = days
            .filter { it.readTimestamp != null }
            .maxByOrNull { it.readTimestamp!! }
        val isWithinWindow = mostRecentReadDay
            ?.readTimestamp
            ?.let { nowMillis - it <= MILESTONE_WINDOW_MILLIS } == true

        resolveEnteredNewTestament(days, mostRecentReadDay, isWithinWindow)
            ?.let { return it }

        resolveOnlyOneBookLeft(booksInPlan, booksWithUnreadPassages, booksFullyRead)
            ?.let { return it }

        if (!isWithinWindow || mostRecentReadDay == null) return null

        val booksClosedByMostRecentDay = mostRecentReadDay.passages
            .map { it.bookId }
            .distinct()
            .filter { it in booksFullyRead }

        if (booksClosedByMostRecentDay.isEmpty()) return null

        if (booksFullyRead.size == 1 && booksClosedByMostRecentDay.size == 1) {
            return Milestone.FirstBookCompleted
        }

        val closedBook = booksClosedByMostRecentDay.maxByOrNull { it.ordinal } ?: return null
        return Milestone.BookCompleted(closedBook)
    }

    private fun resolveEnteredNewTestament(
        days: List<DayModel>,
        mostRecentReadDay: DayModel?,
        isWithinWindow: Boolean,
    ): Milestone? {
        if (!isWithinWindow || mostRecentReadDay == null) return null
        val mostRecentTimestamp = mostRecentReadDay.readTimestamp ?: return null
        val recentHasNewTestament = mostRecentReadDay.passages.any { it.bookId.isNewTestament() }
        if (!recentHasNewTestament) return null

        val earlierReadDays = days
            .filter { day ->
                val ts = day.readTimestamp
                ts != null && ts < mostRecentTimestamp
            }
        val earlierHasNewTestament = earlierReadDays.any { day ->
            day.passages.any { it.bookId.isNewTestament() }
        }
        if (earlierHasNewTestament) return null

        val earlierHasOldTestament = earlierReadDays.any { day ->
            day.passages.any { !it.bookId.isNewTestament() }
        }
        if (!earlierHasOldTestament) return null

        return Milestone.EnteredNewTestament
    }

    private fun resolveOnlyOneBookLeft(
        booksInPlan: List<BookId>,
        booksWithUnreadPassages: List<BookId>,
        booksFullyRead: List<BookId>,
    ): Milestone? {
        if (booksInPlan.size <= 1) return null
        if (booksWithUnreadPassages.size != 1) return null
        if (booksFullyRead.isEmpty()) return null
        return Milestone.OnlyOneBookLeft(booksWithUnreadPassages.single())
    }
}
