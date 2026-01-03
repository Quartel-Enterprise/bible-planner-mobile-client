package com.quare.bibleplanner.core.provider.billing.di

import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsPremiumUserUseCase
import com.quare.bibleplanner.core.provider.billing.model.BillingUnavailableException
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
        IsPremiumUserUseCase { true }
    }
    factory {
        InitializeBillingUseCase {

        }
    }
}

// User is always premium on desktop, since desktop is a premium client.
private fun <T> getResult(): Result<T> = Result.failure(BillingUnavailableException())
