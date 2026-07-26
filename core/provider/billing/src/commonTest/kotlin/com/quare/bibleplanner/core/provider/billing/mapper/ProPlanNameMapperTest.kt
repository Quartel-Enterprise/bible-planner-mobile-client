package com.quare.bibleplanner.core.provider.billing.mapper

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProPlanNameMapperTest {
    private lateinit var mapper: ProPlanNameMapper

    @Test
    fun `should name the monthly plan of every store including the legacy web product`() {
        // Given
        val monthlyProductIds = listOf("com.quare.bibleplanner.premium.monthly", "prod_web_2", "prod_web_1")

        // When
        val planNames = monthlyProductIds.map(mapper::map)

        // Then
        assertEquals(
            expected = List(monthlyProductIds.size) { "Pro / Monthly Plan" },
            actual = planNames,
        )
    }

    @Test
    fun `should name the annual plan of every store including the legacy web product`() {
        // Given
        val annualProductIds = listOf("com.quare.bibleplanner.premium.annual", "prod_web_2_yearly", "prod_web_1_yearly")

        // When
        val planNames = annualProductIds.map(mapper::map)

        // Then
        assertEquals(
            expected = List(annualProductIds.size) { "Pro / Annual Plan" },
            actual = planNames,
        )
    }

    @Test
    fun `should fall back to the product identifier of an unknown product`() {
        // When
        val planName = mapper.map("bibleplanner.premium:bibleplanner-premium-month")

        // Then
        assertEquals(
            expected = "bibleplanner.premium:bibleplanner-premium-month",
            actual = planName,
        )
    }

    @BeforeTest
    fun setUp() {
        mapper = ProPlanNameMapper()
    }
}
