package com.quare.bibleplanner.core.review.domain.usecase

fun interface ShouldRequestReview {
    suspend operator fun invoke(): Boolean
}
