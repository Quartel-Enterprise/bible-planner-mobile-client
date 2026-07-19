package com.quare.bibleplanner.core.review.domain.usecase

import com.quare.bibleplanner.core.review.domain.model.ReviewTrigger

fun interface RequestReviewIfNeeded {
    suspend operator fun invoke(trigger: ReviewTrigger)
}
