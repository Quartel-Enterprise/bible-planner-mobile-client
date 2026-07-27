package com.quare.bibleplanner.core.provider.billing.data.mapper

import com.quare.bibleplanner.core.provider.billing.data.dto.PackageDto
import com.quare.bibleplanner.core.provider.billing.data.dto.PriceDto
import com.quare.bibleplanner.core.provider.billing.data.dto.ProductDto
import com.quare.bibleplanner.core.provider.billing.domain.model.store.StorePackageType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StorePackageMapperTest {
    private lateinit var mapper: StorePackageMapper

    @Test
    fun `should map a monthly package with its product details`() {
        // When
        val storePackage = mapper.map(
            packageDto = packageDto($$"$rc_monthly"),
            product = productDto(),
        )

        // Then
        assertEquals(
            expected = $$"$rc_monthly",
            actual = storePackage.identifier,
        )
        assertEquals(
            expected = StorePackageType.MONTHLY,
            actual = storePackage.type,
        )
        assertEquals(
            expected = 5_900_000L,
            actual = storePackage.priceMicros,
        )
        assertEquals(
            expected = "Bible Planner Pro (Monthly)",
            actual = storePackage.title,
        )
    }

    @Test
    fun `should map an annual package`() {
        // When
        val storePackage = mapper.map(
            packageDto = packageDto($$"$rc_annual"),
            product = productDto(),
        )

        // Then
        assertEquals(
            expected = StorePackageType.ANNUAL,
            actual = storePackage.type,
        )
    }

    @Test
    fun `should map an unknown package identifier to the unknown type`() {
        // When
        val storePackage = mapper.map(
            packageDto = packageDto($$"$rc_weekly"),
            product = productDto(),
        )

        // Then
        assertEquals(
            expected = StorePackageType.UNKNOWN,
            actual = storePackage.type,
        )
    }

    @Test
    fun `should map a missing product description to an empty description`() {
        // When
        val storePackage = mapper.map(
            packageDto = packageDto($$"$rc_monthly"),
            product = productDto(description = null),
        )

        // Then
        assertEquals(
            expected = "",
            actual = storePackage.description,
        )
    }

    @BeforeTest
    fun setUp() {
        mapper = StorePackageMapper(PriceFormatter())
    }

    private fun packageDto(identifier: String): PackageDto = PackageDto(
        identifier = identifier,
        platformProductIdentifier = "prod_web_1",
    )

    private fun productDto(description: String? = "Full access"): ProductDto = ProductDto(
        identifier = "prod_web_1",
        title = "Bible Planner Pro (Monthly)",
        description = description,
        currentPrice = PriceDto(
            amountMicros = 5_900_000L,
            currency = "BRL",
        ),
    )
}
