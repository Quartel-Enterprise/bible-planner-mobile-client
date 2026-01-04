package com.quare.bibleplanner.core.provider.billing.di

import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsInstagramLinkVisibleInMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsInstagramLinkVisibleUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsPremiumUserMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsPremiumUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsPremiumVerificationRequiredUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserInRevenueCatUseCase
import com.quare.bibleplanner.core.provider.billing.mapper.PackageMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val billingProviderModule = module {
    factoryOf(::PackageMapper)
    factoryOf(::GetPurchaseResultMobileUseCase).bind<GetPurchaseResultUseCase>()
    factoryOf(::GetOfferingsResultMobileUseCase).bind<GetOfferingsResultUseCase>()
    factoryOf(::GetRestorePurchaseResultMobileUseCase).bind<GetRestorePurchaseResultUseCase>()
    factoryOf(::IsFreeUserMobileUseCase).bind<IsFreeUserUseCase>()
    factoryOf(::InitializeBillingMobileUseCase).bind<InitializeBillingUseCase>()
    factoryOf(::IsPremiumUserMobileUseCase).bind<IsPremiumUserUseCase>()
    factoryOf(::IsPremiumVerificationRequiredUseCase)
    factoryOf(::IsProUserInRevenueCatUseCase)
    factoryOf(::IsInstagramLinkVisibleInMobileUseCase).bind<IsInstagramLinkVisibleUseCase>()
}
