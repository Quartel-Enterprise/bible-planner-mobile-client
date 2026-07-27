package com.quare.bibleplanner.core.provider.billing.mapper

import com.quare.bibleplanner.core.provider.billing.domain.model.ProPlanType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProPlanTypeMapperTest {
    private lateinit var mapper: ProPlanTypeMapper

    @Test
    fun `should map the monthly product of every store`() {
        // Given
        val monthlyProductIds = listOf(
            "com.quare.bibleplanner.premium.monthly",
            "bibleplanner.premium:bibleplanner-premium-month",
            "prod_web_2",
            "prod_web_1",
        )

        // When
        val planTypes = monthlyProductIds.map(mapper::map)

        // Then
        assertEquals(
            expected = List(monthlyProductIds.size) { ProPlanType.MONTHLY },
            actual = planTypes,
        )
    }

    @Test
    fun `should map the annual product of every store`() {
        // Given
        val annualProductIds = listOf(
            "com.quare.bibleplanner.premium.annual",
            "bibleplanner.premium:bibleplanner-premium-year",
            "prod_web_2_yearly",
            "prod_web_1_yearly",
        )

        // When
        val planTypes = annualProductIds.map(mapper::map)

        // Then
        assertEquals(
            expected = List(annualProductIds.size) { ProPlanType.ANNUAL },
            actual = planTypes,
        )
    }

    @Test
    fun `should map an unrecognized product to unknown`() {
        // When
        val planType = mapper.map("rc_promo_bible_planner_pro")

        // Then
        assertEquals(
            expected = ProPlanType.UNKNOWN,
            actual = planType,
        )
    }

    @BeforeTest
    fun setUp() {
        mapper = ProPlanTypeMapper()
    }
}
