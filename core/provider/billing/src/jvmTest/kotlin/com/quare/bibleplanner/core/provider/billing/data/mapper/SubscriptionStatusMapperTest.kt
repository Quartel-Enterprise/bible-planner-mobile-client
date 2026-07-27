package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.provider.billing.data.dto.freeSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.data.dto.proSubscriberResponse
import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.mapper.ProPlanTypeMapper
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

internal class SubscriptionStatusMapperTest {
    private lateinit var mapper: SubscriptionStatusMapper

    @Test
    fun `should map an active entitlement to Pro`() {
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
    fun `should map an expired entitlement to Free`() {
        // When
        val status = mapper.map(proSubscriberResponse(expiresDate = "2026-01-06T22:23:11Z"))

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `should keep Pro while the grace period is still running`() {
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
    fun `should map an entitlement without expiration to Pro`() {
        // When
        val status = mapper.map(proSubscriberResponse(expiresDate = null))

        // Then
        assertIs<SubscriptionStatus.Pro>(status)
    }

    @Test
    fun `should not renew when the subscription was unsubscribed`() {
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
    fun `should map a subscriber without entitlements to Free`() {
        // When
        val status = mapper.map(freeSubscriberResponse())

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `should map a missing subscriber to Free`() {
        // When
        val status = mapper.map(null)

        // Then
        assertEquals(
            expected = SubscriptionStatus.Free,
            actual = status,
        )
    }

    @Test
    fun `should map the purchase date of an active entitlement`() {
        // When
        val status = mapper.map(proSubscriberResponse())

        // Then
        val pro = assertIs<SubscriptionStatus.Pro>(status)
        assertEquals(
            expected = "2026-01-06T21:23:11Z".toUtcLocalDateTime(),
            actual = pro.purchaseDate,
        )
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
        )
    }

    private fun String.toUtcLocalDateTime(): LocalDateTime = Instant.parse(this).toLocalDateTime(TimeZone.UTC)
}
