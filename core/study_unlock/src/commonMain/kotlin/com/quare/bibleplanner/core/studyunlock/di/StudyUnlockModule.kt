package com.quare.bibleplanner.core.studyunlock.di

import com.quare.bibleplanner.core.studyunlock.domain.store.StudyUnlockResultStore
import com.quare.bibleplanner.core.studyunlock.domain.usecase.GatherAdsConsentIfEnabled
import com.quare.bibleplanner.core.studyunlock.domain.usecase.IsRewardedUnlockEnabled
import com.quare.bibleplanner.core.studyunlock.domain.usecase.PrepareRewardedUnlockOffer
import com.quare.bibleplanner.core.studyunlock.domain.usecase.impl.GatherAdsConsentIfEnabledUseCase
import com.quare.bibleplanner.core.studyunlock.domain.usecase.impl.IsRewardedUnlockEnabledUseCase
import com.quare.bibleplanner.core.studyunlock.domain.usecase.impl.PrepareRewardedUnlockOfferUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val studyUnlockModule = module {
    singleOf(::StudyUnlockResultStore)
    factoryOf(::IsRewardedUnlockEnabledUseCase).bind<IsRewardedUnlockEnabled>()
    factoryOf(::PrepareRewardedUnlockOfferUseCase).bind<PrepareRewardedUnlockOffer>()
    factoryOf(::GatherAdsConsentIfEnabledUseCase).bind<GatherAdsConsentIfEnabled>()
}
