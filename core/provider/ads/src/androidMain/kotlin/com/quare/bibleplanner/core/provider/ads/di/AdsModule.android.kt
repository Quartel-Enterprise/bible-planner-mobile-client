package com.quare.bibleplanner.core.provider.ads.di

import com.quare.bibleplanner.core.provider.ads.data.service.AndroidAdsConsentDataSource
import com.quare.bibleplanner.core.provider.ads.data.service.AndroidRewardedAdDataSource
import com.quare.bibleplanner.core.provider.ads.data.service.AndroidRewardedAdUnitIdProvider
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdUnitIdProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAdsModule: Module = module {
    singleOf(::AndroidRewardedAdDataSource).bind<RewardedAdDataSource>()
    singleOf(::AndroidAdsConsentDataSource).bind<AdsConsentDataSource>()
    factoryOf(::AndroidRewardedAdUnitIdProvider).bind<RewardedAdUnitIdProvider>()
}
