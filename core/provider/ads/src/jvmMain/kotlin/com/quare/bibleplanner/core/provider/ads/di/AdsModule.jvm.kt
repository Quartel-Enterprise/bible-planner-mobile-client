package com.quare.bibleplanner.core.provider.ads.di

import com.quare.bibleplanner.core.provider.ads.data.service.UnsupportedAdsConsentDataSource
import com.quare.bibleplanner.core.provider.ads.data.service.UnsupportedRewardedAdDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdUnitIdProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAdsModule: Module = module {
    singleOf(::UnsupportedRewardedAdDataSource).bind<RewardedAdDataSource>()
    singleOf(::UnsupportedAdsConsentDataSource).bind<AdsConsentDataSource>()
    factory<RewardedAdUnitIdProvider> { RewardedAdUnitIdProvider { "" } }
}
