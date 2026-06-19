package com.quare.bibleplanner.core.provider.billing.di

import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetSubscriptionStatusFlowUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisible
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveProVerificationRequired
import com.quare.bibleplanner.core.provider.billing.domain.usecase.SyncBillingUserId
import com.quare.bibleplanner.core.provider.billing.model.BillingUnavailableException
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

actual val billingProviderModule = module {
    factory {
        GetOfferingsResultUseCase { getResult() }
    }
    factory {
        GetPurchaseResultUseCase { getResult() }
    }
    factory {
        GetRestorePurchaseResultUseCase { getResult() }
    }
    factory {
        IsFreeUserUseCase { false }
    }
    factory {
        IsProUserUseCase { true }
    }
    factory {
        InitializeBillingUseCase {
        }
    }
    factory {
        ObserveInstagramLinkVisible { flowOf(true) }
    }
    factory {
        ObserveProVerificationRequired { flowOf(false) }
    }
    factory {
        GetSubscriptionStatusFlowUseCase { flowOf(null) }
    }
    factory {
        SyncBillingUserId { }
    }
    factory {
        ObserveIsProUser { flowOf(true) }
    }
}

// User is always premium on desktop, since desktop is a premium client.
private fun <T> getResult(): Result<T> = Result.failure(BillingUnavailableException())
