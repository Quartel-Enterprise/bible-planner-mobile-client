package com.quare.bibleplanner.core.provider.ads.di

import com.quare.bibleplanner.core.provider.ads.data.service.IosRewardedAdUnitIdProvider
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdUnitIdProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAdsModule: Module = module {
    factoryOf(::IosRewardedAdUnitIdProvider).bind<RewardedAdUnitIdProvider>()
}
