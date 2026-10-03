package com.quare.bibleplanner.core.provider.billing.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import com.quare.bibleplanner.core.provider.billing.data.datasource.RevenueCatRestDataSource
import com.quare.bibleplanner.core.provider.billing.data.dto.PackageDto
import com.quare.bibleplanner.core.provider.billing.data.mapper.StorePackageMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.SubscriptionStatusMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.WebPurchaseLinkBuilder
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackage
import com.quare.bibleplanner.core.provider.billing.domain.repository.DesktopBillingRepository
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DesktopBillingRepositoryImpl(
    private val revenueCatRestDataSource: RevenueCatRestDataSource,
    private val subscriptionStatusMapper: SubscriptionStatusMapper,
    private val storePackageMapper: StorePackageMapper,
    private val webPurchaseLinkBuilder: WebPurchaseLinkBuilder,
    private val config: DesktopBillingConfig,
) : DesktopBillingRepository {
    override val subscriptionStatus: StateFlow<SubscriptionStatus?>
        field = MutableStateFlow<SubscriptionStatus?>(null)

    override suspend fun refreshSubscriptionStatus(): SubscriptionStatus {
        if (!config.isEntitlementReadEnabled) return SubscriptionStatus.Free
        val status = suspendRunCatching {
            subscriptionStatusMapper.map(revenueCatRestDataSource.getSubscriber())
        }.onFailure { throwable ->
            Logger.e(throwable) { "Failed to fetch the RevenueCat subscriber" }
        }.getOrElse { subscriptionStatus.value ?: SubscriptionStatus.Free }
        subscriptionStatus.value = status
        return status
    }

    override fun clearSubscriptionStatus() {
        subscriptionStatus.value = null
    }

    override suspend fun getStorePackages(): List<StorePackage> {
        if (!config.isPurchaseEnabled) throw BillingUnavailableException()
        val currentPackages = getCurrentPackages()
        if (currentPackages.isEmpty()) return emptyList()
        val products = revenueCatRestDataSource
            .getProducts(currentPackages.map(PackageDto::platformProductIdentifier))
            .productDetails
            .associateBy { product -> product.identifier }
        return currentPackages.mapNotNull { packageDto ->
            products[packageDto.platformProductIdentifier]?.let { product ->
                storePackageMapper.map(
                    packageDto = packageDto,
                    product = product,
                )
            }
        }
    }

    override fun getCheckoutUrl(
        appUserId: String,
        packageIdentifier: String,
    ): String {
        if (!config.isPurchaseEnabled) throw BillingUnavailableException()
        return webPurchaseLinkBuilder.build(
            appUserId = appUserId,
            packageIdentifier = packageIdentifier,
        )
    }

    private suspend fun getCurrentPackages(): List<PackageDto> {
        val offerings = revenueCatRestDataSource.getOfferings()
        return offerings.offerings
            .firstOrNull { offering -> offering.identifier == offerings.currentOfferingId }
            ?.packages
            .orEmpty()
    }
}
