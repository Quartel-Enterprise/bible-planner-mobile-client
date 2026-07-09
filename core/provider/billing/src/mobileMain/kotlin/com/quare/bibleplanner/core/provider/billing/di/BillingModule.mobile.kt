package com.quare.bibleplanner.core.provider.billing.di

import com.quare.bibleplanner.core.provider.billing.domain.source.BillingCustomerInfoSource
import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccount
import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccountImpl
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCaseImpl
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisible
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisibleInMobileUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveStoreSubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveStoreSubscriptionStatusUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.SyncBillingUserId
import com.quare.bibleplanner.core.provider.billing.domain.usecase.SyncBillingUserIdUseCase
import com.quare.bibleplanner.core.provider.billing.mapper.PackageMapper
import com.revenuecat.purchases.kmp.Purchases
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val billingProviderModule = module {
    single<Purchases> { Purchases.sharedInstance }
    singleOf(::BillingCustomerInfoSource)
    factoryOf(::PackageMapper)
    factoryOf(::GetPurchaseResultMobileUseCase).bind<GetPurchaseResultUseCase>()
    factoryOf(::GetOfferingsResultMobileUseCase).bind<GetOfferingsResultUseCase>()
    factoryOf(::GetRestorePurchaseResultMobileUseCase).bind<GetRestorePurchaseResultUseCase>()
    factoryOf(::IsFreeUserMobileUseCase).bind<IsFreeUserUseCase>()
    factoryOf(::InitializeBillingMobileUseCase).bind<InitializeBillingUseCase>()
    factoryOf(::IsProUserMobileUseCase).bind<IsProUserUseCase>()
    factoryOf(::ObserveInstagramLinkVisibleInMobileUseCase).bind<ObserveInstagramLinkVisible>()
    factoryOf(::ObserveStoreSubscriptionStatusUseCase).bind<ObserveStoreSubscriptionStatus>()
    factoryOf(::GetSubscriptionStatusFlowUseCaseImpl).bind<GetSubscriptionStatusFlowUseCase>()
    factoryOf(::ObserveIsProUserUseCase).bind<ObserveIsProUser>()
    factoryOf(::BillingUserAccountImpl).bind<BillingUserAccount>()
    factoryOf(::SyncBillingUserIdUseCase).bind<SyncBillingUserId>()
}
