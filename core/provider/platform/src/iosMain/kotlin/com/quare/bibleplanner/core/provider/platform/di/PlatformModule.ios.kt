package com.quare.bibleplanner.core.provider.platform.di

import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformReviewModule: Module = module {
    factory<RequestInAppReview> { RequestInAppReview { false } }
}
