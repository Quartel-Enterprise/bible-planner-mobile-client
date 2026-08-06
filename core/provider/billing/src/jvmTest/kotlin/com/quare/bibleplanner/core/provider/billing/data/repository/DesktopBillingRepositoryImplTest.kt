package com.quare.bibleplanner.core.provider.billing.data.repository

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.provider.billing.data.config.DesktopBillingConfig
import com.quare.bibleplanner.core.provider.billing.data.datasource.RevenueCatRestDataSource
import com.quare.bibleplanner.core.provider.billing.data.dto.OfferingDto
import com.quare.bibleplanner.core.provider.billing.data.dto.OfferingsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.PackageDto
import com.quare.bibleplanner.core.provider.billing.data.dto.PriceDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductsResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.SubscriberResponseDto
import com.quare.bibleplanner.core.provider.billing.data.dto.TEST_APP_USER_ID
import com.quare.bibleplanner.core.provider.billing.data.dto.freeSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.data.dto.proSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.data.mapper.EpochMillisMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.PriceFormatter
import com.quare.bibleplanner.core.provider.billing.data.mapper.ProEntitlementMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.PurchaseStoreMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.StorePackageMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.SubscriptionStatusMapper
import com.quare.bibleplanner.core.provider.billing.data.mapper.WebPurchaseLinkBuilder
import com.quare.bibleplanner.core.provider.billing.domain.model.BillingUnavailableException
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.mapper.ProPlanTypeMapper
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

private const val MONTHLY_PACKAGE_IDENTIFIER = $$"$rc_monthly"
private const val ANNUAL_PACKAGE_IDENTIFIER = $$"$rc_annual"
private const val MONTHLY_PRODUCT_ID = "prod_web_1"
private const val ANNUAL_PRODUCT_ID = "prod_web_1_yearly"
private const val PURCHASE_LINK = "https://pay.rev.cat/token"

internal class DesktopBillingRepositoryImplTest {
    private lateinit var repository: DesktopBillingRepositoryImpl
    private lateinit var revenueCatRestDataSource: FakeRevenueCatRestDataSource

    @Test
    fun `should map the current offering packages to store packages`() = runTest {
        // Given
        prepareScenario()

        // When
        val storePackages = repository.getStorePackages()

        // Then
        assertEquals(
            expected = listOf(MONTHLY_PACKAGE_IDENTIFIER, ANNUAL_PACKAGE_IDENTIFIER),
            actual = storePackages.map { storePackage -> storePackage.identifier },
        )
        assertEquals(
            expected = listOf(5_900_000L, 49_900_000L),
            actual = storePackages.map { storePackage -> storePackage.priceMicros },
        )
        assertEquals(
            expected = "Bible Planner Pro (Monthly)",
            actual = storePackages.first().title,
        )
    }

    @Test
    fun `should ignore packages without a matching product`() = runTest {
        // Given
        prepareScenario(availableProductIds = listOf(MONTHLY_PRODUCT_ID))

        // When
        val storePackages = repository.getStorePackages()

        // Then
        assertEquals(
            expected = listOf(MONTHLY_PACKAGE_IDENTIFIER),
            actual = storePackages.map { storePackage -> storePackage.identifier },
        )
    }

    @Test
    fun `should return no store packages when the current offering is unknown`() = runTest {
        // Given
        prepareScenario(currentOfferingId = "unknown")

        // When
        val storePackages = repository.getStorePackages()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = storePackages,
        )
    }

    @Test
    fun `should not expose store packages when the purchase link is missing`() = runTest {
        // Given
        prepareScenario(purchaseLink = "")

        // When & Then
        assertFailsWith<BillingUnavailableException> { repository.getStorePackages() }
    }

    @Test
    fun `should build the checkout url for the given app user id`() = runTest {
        // Given
        prepareScenario()

        // When
        val url = repository.getCheckoutUrl(
            appUserId = TEST_APP_USER_ID,
            packageIdentifier = MONTHLY_PACKAGE_IDENTIFIER,
        )

        // Then
        assertEquals(
            expected = "$PURCHASE_LINK/$TEST_APP_USER_ID?package_id=%24rc_monthly",
            actual = url,
        )
    }

    @Test
    fun `should cache the subscription status of an active subscriber`() = runTest {
        // Given
        prepareScenario(subscriber = proSubscriberResponse())

        // When
        val status = repository.refreshSubscriptionStatus()

        // Then
        assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(
            expected = status,
            actual = repository.subscriptionStatus.value,
        )
    }

    @Test
    fun `should report Free without calling the api when the api key is missing`() = runTest {
        // Given
        prepareScenario(
            apiKey = "",
            subscriber = null,
        )

        // When
        val status = repository.refreshSubscriptionStatus()

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `should keep the last known status when the request fails`() = runTest {
        // Given
        prepareScenario(subscriber = proSubscriberResponse())
        repository.refreshSubscriptionStatus()
        revenueCatRestDataSource.subscriber = null

        // When
        val status = repository.refreshSubscriptionStatus()

        // Then
        assertIs<SubscriptionStatus.Pro>(status)
    }

    @Test
    fun `should report Free when the first request fails`() = runTest {
        // Given
        prepareScenario(subscriber = null)

        // When
        val status = repository.refreshSubscriptionStatus()

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `should drop the cached status on clear`() = runTest {
        // Given
        prepareScenario(subscriber = freeSubscriberResponse())
        repository.refreshSubscriptionStatus()

        // When
        repository.clearSubscriptionStatus()

        // Then
        assertNull(repository.subscriptionStatus.value)
    }

    private fun prepareScenario(
        currentOfferingId: String = "default",
        availableProductIds: List<String> = listOf(MONTHLY_PRODUCT_ID, ANNUAL_PRODUCT_ID),
        apiKey: String = "rcb_test",
        purchaseLink: String = PURCHASE_LINK,
        subscriber: SubscriberResponseDto? = freeSubscriberResponse(),
    ) {
        val config = DesktopBillingConfig(
            apiKey = apiKey,
            purchaseLink = purchaseLink,
        )
        revenueCatRestDataSource = FakeRevenueCatRestDataSource(
            currentOfferingId = currentOfferingId,
            availableProductIds = availableProductIds,
            subscriber = subscriber,
        )
        repository = DesktopBillingRepositoryImpl(
            revenueCatRestDataSource = revenueCatRestDataSource,
            subscriptionStatusMapper = SubscriptionStatusMapper(
                localDateTimeProvider = LocalDateTimeProvider { timestamp ->
                    Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.UTC)
                },
                proPlanTypeMapper = ProPlanTypeMapper(),
                proEntitlementMapper = ProEntitlementMapper(EpochMillisMapper()),
                epochMillisMapper = EpochMillisMapper(),
                purchaseStoreMapper = PurchaseStoreMapper(),
            ),
            storePackageMapper = StorePackageMapper(PriceFormatter()),
            webPurchaseLinkBuilder = WebPurchaseLinkBuilder(config),
            config = config,
        )
    }
}

private class FakeRevenueCatRestDataSource(
    private val currentOfferingId: String,
    private val availableProductIds: List<String>,
    var subscriber: SubscriberResponseDto?,
) : RevenueCatRestDataSource {
    override suspend fun getSubscriber(): SubscriberResponseDto = subscriber
        ?: error("subscriber unavailable")

    override suspend fun getOfferings(): OfferingsResponseDto = OfferingsResponseDto(
        currentOfferingId = currentOfferingId,
        offerings = listOf(
            OfferingDto(
                identifier = "default",
                packages = listOf(
                    PackageDto(
                        identifier = MONTHLY_PACKAGE_IDENTIFIER,
                        platformProductIdentifier = MONTHLY_PRODUCT_ID,
                    ),
                    PackageDto(
                        identifier = ANNUAL_PACKAGE_IDENTIFIER,
                        platformProductIdentifier = ANNUAL_PRODUCT_ID,
                    ),
                ),
            ),
        ),
    )

    override suspend fun getProducts(productIds: List<String>): ProductsResponseDto = ProductsResponseDto(
        productDetails = productIds
            .filter { productId -> productId in availableProductIds }
            .map { productId ->
                ProductDto(
                    identifier = productId,
                    title = if (productId == MONTHLY_PRODUCT_ID) {
                        "Bible Planner Pro (Monthly)"
                    } else {
                        "Bible Planner Pro (Yearly)"
                    },
                    description = null,
                    currentPrice = PriceDto(
                        amountMicros = if (productId == MONTHLY_PRODUCT_ID) 5_900_000L else 49_900_000L,
                        currency = "BRL",
                    ),
                )
            },
    )
}
