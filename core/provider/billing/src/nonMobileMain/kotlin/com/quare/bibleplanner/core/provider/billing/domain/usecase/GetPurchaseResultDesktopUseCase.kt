package com.quare.bibleplanner.core.provider.billing.domain.usecase

import com.quare.bibleplanner.core.provider.billing.domain.model.BillingException
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class GetPurchaseResultDesktopUseCase(
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
    private val repository: DesktopBillingRepository,
    private val openUrl: OpenUrl,
    private val awaitProEntitlement: AwaitProEntitlementUseCase,
) : GetPurchaseResultUseCase {
    override suspend fun invoke(storePackage: StorePackage): Result<Unit> = suspendRunCatching {
        val appUserId = getAuthenticatedUserId() ?: throw BillingUnavailableException()
        openUrl(
            repository.getCheckoutUrl(
                appUserId = appUserId,
                packageIdentifier = storePackage.identifier,
            ),
        )
        if (!awaitProEntitlement()) throw BillingException.BrowserCheckoutNotConfirmed()
    }
}
