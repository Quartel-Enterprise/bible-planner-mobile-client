package com.quare.bibleplanner.core.provider.ads.di

import com.quare.bibleplanner.core.provider.ads.data.service.AdsConsentServiceImpl
import com.quare.bibleplanner.core.provider.ads.data.service.RewardedAdServiceImpl
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentService
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdService
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val adsModule = module {
    includes(platformAdsModule)
    singleOf(::AdsConsentServiceImpl).bind<AdsConsentService>()
    singleOf(::RewardedAdServiceImpl).bind<RewardedAdService>()
}

internal expect val platformAdsModule: Module
