package com.quare.bibleplanner.core.provider.billing.di

import com.quare.bibleplanner.core.provider.billing.data.browser.SystemBrowserUrlOpener
import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfigProvider
import com.quare.bibleplanner.core.provider.billing.data.datasource.AnonymousAppUserIdProvider
import com.quare.bibleplanner.core.provider.billing.data.datasource.AppUserIdProvider
import com.quare.bibleplanner.core.provider.billing.data.datasource.GetAnonymousAppUserId
import com.quare.bibleplanner.core.provider.billing.data.datasource.GetAppUserId
import com.quare.bibleplanner.core.provider.billing.data.datasource.RevenueCatRestDataSource
import com.quare.bibleplanner.core.provider.billing.data.datasource.RevenueCatRestDataSourceImpl
import com.quare.bibleplanner.core.provider.billing.data.datasource.createRevenueCatHttpClient
import com.quare.bibleplanner.core.provider.billing.data.mapper.EpochMillisMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.PriceFormatter
import com.quare.bibleplanner.core.provider.billing.data.mapper.ProEntitlementMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.StorePackageMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.SubscriptionStatusMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.WebPurchaseLinkBuilder
import com.quare.bibleplanner.core.provider.billing.data.repository.DesktopBillingRepositoryImpl
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import com.quare.bibleplanner.core.provider.billing.domain.usecase.AwaitProEntitlementUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccount
import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccountDesktop
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultDesktopUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultDesktopUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultDesktopUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCaseImpl
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCaseImpl
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserDesktopUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisible
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveStoreSubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveStoreSubscriptionStatusDesktopUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.OpenUrl
import com.quare.bibleplanner.core.provider.billing.domain.usecase.SyncBillingUserId
import com.quare.bibleplanner.core.provider.billing.domain.usecase.SyncBillingUserIdUseCase
import com.quare.bibleplanner.core.provider.billing.mapper.ProPlanNameMapper
import com.quare.bibleplanner.core.provider.platform.isDebugBuild
import kotlinx.coroutines.flow.flowOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val billingProviderModule = module {
    single { DesktopBillingConfigProvider(isDebugBuild = isDebugBuild()).getConfig() }
    singleOf(::AnonymousAppUserIdProvider).bind<GetAnonymousAppUserId>()
    singleOf(::AppUserIdProvider).bind<GetAppUserId>()
    single<RevenueCatRestDataSource> {
        RevenueCatRestDataSourceImpl(
            httpClient = createRevenueCatHttpClient(config = get()),
            getAppUserId = get(),
        )
    }
    singleOf(::DesktopBillingRepositoryImpl).bind<DesktopBillingRepository>()
    factoryOf(::EpochMillisMapper)
    factoryOf(::PriceFormatter)
    factoryOf(::ProEntitlementMapper)
    factoryOf(::StorePackageMapper)
    factoryOf(::ProPlanNameMapper)
    factoryOf(::SubscriptionStatusMapper)
    factoryOf(::WebPurchaseLinkBuilder)
    factoryOf(::SystemBrowserUrlOpener).bind<OpenUrl>()
    factoryOf(::AwaitProEntitlementUseCase)
    factoryOf(::GetOfferingsResultDesktopUseCase).bind<GetOfferingsResultUseCase>()
    factoryOf(::GetPurchaseResultDesktopUseCase).bind<GetPurchaseResultUseCase>()
    factoryOf(::GetRestorePurchaseResultDesktopUseCase).bind<GetRestorePurchaseResultUseCase>()
    factoryOf(::IsProUserDesktopUseCase).bind<IsProUserUseCase>()
    factoryOf(::IsFreeUserUseCaseImpl).bind<IsFreeUserUseCase>()
    factoryOf(::ObserveStoreSubscriptionStatusDesktopUseCase).bind<ObserveStoreSubscriptionStatus>()
    factoryOf(::GetSubscriptionStatusFlowUseCaseImpl).bind<GetSubscriptionStatusFlowUseCase>()
    factoryOf(::ObserveIsProUserUseCase).bind<ObserveIsProUser>()
    factoryOf(::BillingUserAccountDesktop).bind<BillingUserAccount>()
    factoryOf(::SyncBillingUserIdUseCase).bind<SyncBillingUserId>()
    factory {
        InitializeBillingUseCase {
        }
    }
    factory {
        ObserveInstagramLinkVisible { flowOf(true) }
    }
}
