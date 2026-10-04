package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.provider.billing.data.dto.freeSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.data.dto.proSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.PurchaseStore
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.mapper.ProPlanTypeMapper
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

internal class SubscriptionStatusMapperTest {
    private lateinit var mapper: SubscriptionStatusMapper

    @Test
    fun `GIVEN an active entitlement WHEN mapping THEN returns Pro`() {
        // When
        val status = mapper.map(proSubscriberResponse())

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(
            expected = ProPlanType.MONTHLY,
            actual = pro.planType,
        )
        assertEquals(
            expected = true,
            actual = pro.willRenew,
        )
    }

    @Test
    fun `GIVEN an expired entitlement WHEN mapping THEN returns Free`() {
        // When
        val status = mapper.map(proSubscriberResponse(expiresDate = "2026-01-06T22:23:11Z"))

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `GIVEN an expired entitlement with a running grace period WHEN mapping THEN returns Pro`() {
        // When
        val status = mapper.map(
            proSubscriberResponse(
                expiresDate = "2026-01-06T22:23:11Z",
                gracePeriodExpiresDate = "2099-01-06T22:23:11Z",
            ),
        )

        // Then
        assertIs<SubscriptionStatus.Pro>(status)
    }

    @Test
    fun `GIVEN an entitlement without expiration WHEN mapping THEN returns Pro`() {
        // When
        val status = mapper.map(proSubscriberResponse(expiresDate = null))

        // Then
        assertIs<SubscriptionStatus.Pro>(status)
    }

    @Test
    fun `GIVEN an unsubscribed subscription WHEN mapping THEN returns Pro that does not renew`() {
        // When
        val status = mapper.map(proSubscriberResponse(unsubscribeDetectedAt = "2026-01-06T18:24:02Z"))

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(
            expected = false,
            actual = pro.willRenew,
        )
    }

    @Test
    fun `GIVEN a subscriber without entitlements WHEN mapping THEN returns Free`() {
        // When
        val status = mapper.map(freeSubscriberResponse())

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `GIVEN a missing subscriber WHEN mapping THEN returns Free`() {
        // When
        val status = mapper.map(null)

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `GIVEN an active entitlement WHEN mapping THEN maps its purchase date`() {
        // When
        val status = mapper.map(proSubscriberResponse())

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(
            expected = "2026-01-06T21:23:11Z".toUtcLocalDateTime(),
            actual = pro.purchaseDate,
        )
    }

    @Test
    fun `GIVEN an entitlement granted by the Play Store WHEN mapping THEN maps that store`() {
        // When
        val status = mapper.map(proSubscriberResponse(store = "play_store"))

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(expected = PurchaseStore.PLAY_STORE, actual = pro.store)
    }

    @Test
    fun `GIVEN an entitlement granted by web billing WHEN mapping THEN maps the store as web`() {
        // When
        val status = mapper.map(proSubscriberResponse(store = "rc_billing"))

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(expected = PurchaseStore.WEB, actual = pro.store)
    }

    @Test
    fun `GIVEN an entitlement without a purchase store WHEN mapping THEN maps the store as null`() {
        // When
        val status = mapper.map(proSubscriberResponse(store = "promotional"))

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertNull(pro.store)
    }

    @BeforeTest
    fun setUp() {
        mapper = SubscriptionStatusMapper(
            localDateTimeProvider = LocalDateTimeProvider { timestamp ->
                Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.UTC)
            },
            proPlanTypeMapper = ProPlanTypeMapper(),
            proEntitlementMapper = ProEntitlementMapper(EpochMillisMapper()),
            epochMillisMapper = EpochMillisMapper(),
            purchaseStoreMapper = PurchaseStoreMapper(),
        )
    }

    private fun String.toUtcLocalDateTime(): LocalDateTime = Instant.parse(this).toLocalDateTime(TimeZone.UTC)
}
