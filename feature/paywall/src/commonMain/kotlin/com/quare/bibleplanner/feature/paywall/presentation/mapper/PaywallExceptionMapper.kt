package com.quare.bibleplanner.feature.paywall.presentation.mapper

import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.error_network
import bibleplanner.feature.paywall.generated.resources.error_payment_pending
import bibleplanner.feature.paywall.generated.resources.error_purchase_cancelled
import bibleplanner.feature.paywall.generated.resources.error_restore_purchase
import bibleplanner.feature.paywall.generated.resources.error_unknown
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import org.jetbrains.compose.resources.StringResource

class PaywallExceptionMapper {
    fun map(throwable: Throwable): StringResource = when (throwable) {
        is BillingException.UserCancelled -> Res.string.error_purchase_cancelled
        is BillingException.NetworkError -> Res.string.error_network
        is BillingException.PaymentPending -> Res.string.error_payment_pending
        is BillingException.RestorePurchaseFailed -> Res.string.error_restore_purchase
        else -> Res.string.error_unknown
    }
}
