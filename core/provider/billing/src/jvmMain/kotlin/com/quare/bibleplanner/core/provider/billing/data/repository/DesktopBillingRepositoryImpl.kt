package com.quare.bibleplanner.core.provider.billing.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import com.quare.bibleplanner.core.provider.billing.data.datasource.GetAppUserId
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
import kotlinx.coroutines.flow.asStateFlow

internal class DesktopBillingRepositoryImpl(
    private val revenueCatRestDataSource: RevenueCatRestDataSource,
    private val getAppUserId: GetAppUserId,
    private val subscriptionStatusMapper: SubscriptionStatusMapper,
    private val storePackageMapper: StorePackageMapper,
    private val webPurchaseLinkBuilder: WebPurchaseLinkBuilder,
    private val config: DesktopBillingConfig,
) : DesktopBillingRepository {
    private val _subscriptionStatus: MutableStateFlow<SubscriptionStatus?> = MutableStateFlow(null)
    override val subscriptionStatus: StateFlow<SubscriptionStatus?> = _subscriptionStatus.asStateFlow()

    override suspend fun refreshSubscriptionStatus(): SubscriptionStatus {
        if (!config.isEntitlementReadEnabled) return SubscriptionStatus.Free
        val status = suspendRunCatching {
            subscriptionStatusMapper.map(revenueCatRestDataSource.getSubscriber(getAppUserId()))
        }.onFailure { throwable ->
            Logger.e(throwable) { "Failed to fetch the RevenueCat subscriber" }
        }.getOrElse { _subscriptionStatus.value ?: SubscriptionStatus.Free }
        _subscriptionStatus.value = status
        return status
    }

    override fun clearSubscriptionStatus() {
        _subscriptionStatus.value = null
    }

    override suspend fun getStorePackages(): List<StorePackage> {
        if (!config.isPurchaseEnabled) throw BillingUnavailableException()
        val appUserId = getAppUserId()
        val currentPackages = getCurrentPackages(appUserId)
        if (currentPackages.isEmpty()) return emptyList()
        val products = revenueCatRestDataSource
            .getProducts(
                appUserId = appUserId,
                productIds = currentPackages.map(PackageDto::platformProductIdentifier),
            ).productDetails
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

    override fun getWebCheckoutUrl(
        appUserId: String,
        packageIdentifier: String,
    ): String {
        if (!config.isPurchaseEnabled) throw BillingUnavailableException()
        return webPurchaseLinkBuilder.build(
            appUserId = appUserId,
            packageIdentifier = packageIdentifier,
        )
    }

    private suspend fun getCurrentPackages(appUserId: String): List<PackageDto> {
        val offerings = revenueCatRestDataSource.getOfferings(appUserId)
        return offerings.offerings
            .firstOrNull { offering -> offering.identifier == offerings.currentOfferingId }
            ?.packages
            .orEmpty()
    }
}
