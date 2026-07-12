package com.quare.bibleplanner.core.provider.platform.domain.usecase

fun interface RequestInAppReview {
    suspend operator fun invoke(): Boolean
}
