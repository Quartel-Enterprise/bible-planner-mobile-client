package com.quare.bibleplanner.core.plan.domain

import com.quare.bibleplanner.core.model.plan.DayModel

/*
 * Why: the streak is the run of read days from the plan start up to the first unread one, so
 * reading ahead past a gap never counts.
 */
val List<DayModel>.readingStreak: Int
    get() = indexOfFirst { !it.isRead }.takeIf { it >= 0 } ?: size

/*
 * Why: the plan screen hides the streak once the plan is finished or the reader is behind today's
 * day, so every other place that shows it follows the same rule.
 */
val List<DayModel>.shownReadingStreak: Int
    get() {
        val firstUnreadIndex = indexOfFirst { !it.isRead }
        val todayIndex = indexOfFirst { it.isToday }
        val isBehind = todayIndex >= 0 && firstUnreadIndex < todayIndex
        return if (firstUnreadIndex < 0 || isBehind) 0 else firstUnreadIndex
    }
