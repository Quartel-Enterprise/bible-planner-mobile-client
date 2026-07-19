package com.quare.bibleplanner.core.review.domain.model

enum class ReviewTrigger(
    val analyticsValue: String,
) {
    STREAK_MILESTONE("streak_milestone"),
    PROGRESS_MILESTONE("progress_milestone"),
    BOOK_COMPLETED("book_completed"),
}
