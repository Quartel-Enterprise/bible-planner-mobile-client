package com.quare.bibleplanner.core.review.di

import com.quare.bibleplanner.core.review.data.ReviewPreferencesImpl
import com.quare.bibleplanner.core.review.domain.ReviewPreferences
import com.quare.bibleplanner.core.review.domain.usecase.RequestReviewIfNeeded
import com.quare.bibleplanner.core.review.domain.usecase.ShouldRequestReview
import com.quare.bibleplanner.core.review.domain.usecase.impl.RequestReviewIfNeededUseCase
import com.quare.bibleplanner.core.review.domain.usecase.impl.ShouldRequestReviewUseCase
import com.quare.bibleplanner.core.review.generated.ReviewBuildKonfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val reviewModule = module {
    // Data
    singleOf(::ReviewPreferencesImpl).bind<ReviewPreferences>()

    // Domain
    factory<ShouldRequestReview> {
        ShouldRequestReviewUseCase(
            reviewPreferences = get(),
            currentTimestampProvider = get(),
            appVersion = ReviewBuildKonfig.APP_VERSION,
        )
    }
    factory<RequestReviewIfNeeded> {
        RequestReviewIfNeededUseCase(
            shouldRequestReview = get(),
            requestInAppReview = get(),
            reviewPreferences = get(),
            currentTimestampProvider = get(),
            trackEvent = get(),
            appVersion = ReviewBuildKonfig.APP_VERSION,
        )
    }
}
