package com.quare.bibleplanner.feature.paywall.presentation.component.description

import androidx.compose.runtime.Composable
import bibleplanner.feature.paywall.generated.resources.Res
import bibleplanner.feature.paywall.generated.resources.footer_payment_info
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SecurePaymentDescription(storeName: String) {
    BaseTextDescription(
        text = stringResource(Res.string.footer_payment_info, storeName),
    )
}
