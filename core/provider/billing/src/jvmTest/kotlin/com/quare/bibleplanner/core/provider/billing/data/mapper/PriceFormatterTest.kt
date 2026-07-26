package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.PriceDto
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PriceFormatterTest {
    private val defaultLocale: Locale = Locale.getDefault()
    private lateinit var formatter: PriceFormatter

    @Test
    fun `should format the amount with the product currency`() {
        // When
        val price = formatter.format(
            PriceDto(
                amountMicros = 5_900_000L,
                currency = "BRL",
            ),
        )

        // Then
        assertEquals(
            expected = "R$5.90",
            actual = price,
        )
    }

    @Test
    fun `should format an amount without decimals`() {
        // When
        val price = formatter.format(
            PriceDto(
                amountMicros = 49_000_000L,
                currency = "USD",
            ),
        )

        // Then
        assertEquals(
            expected = "$49.00",
            actual = price,
        )
    }

    @Test
    fun `should fall back to the raw currency code when it is unknown`() {
        // When
        val price = formatter.format(
            PriceDto(
                amountMicros = 5_900_000L,
                currency = "not-a-currency",
            ),
        )

        // Then
        assertTrue(price.startsWith("not-a-currency"))
        assertTrue(price.contains("5.9"))
    }

    @BeforeTest
    fun setUp() {
        Locale.setDefault(Locale.US)
        formatter = PriceFormatter()
    }

    @AfterTest
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }
}
