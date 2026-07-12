package com.quare.bibleplanner.core.provider.platform.di

import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.quare.bibleplanner.core.provider.platform.AndroidRequestInAppReview
import com.quare.bibleplanner.core.provider.platform.CurrentActivityProvider
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformReviewModule: Module = module {
    single<ReviewManager> { ReviewManagerFactory.create(androidContext()) }
    singleOf(::CurrentActivityProvider)
    factoryOf(::AndroidRequestInAppReview).bind<RequestInAppReview>()
}
