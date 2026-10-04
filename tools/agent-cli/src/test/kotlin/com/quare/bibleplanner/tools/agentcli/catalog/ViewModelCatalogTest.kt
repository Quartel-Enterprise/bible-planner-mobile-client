package com.quare.bibleplanner.tools.agentcli.catalog

import com.quare.bibleplanner.tools.agentcli.fake.AppLevelViewModel
import com.quare.bibleplanner.tools.agentcli.fake.SampleViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class ViewModelCatalogTest {
    @Test
    fun `finds a ViewModel by its simple name`() {
        // Given
        val catalog = ViewModelCatalog(listOf(SampleViewModel::class, AppLevelViewModel::class))

        // When
        val viewModels = listOf(catalog.find("SampleViewModel"), catalog.findOrNull("AppLevelViewModel"))

        // Then
        assertEquals(
            expected = listOf(SampleViewModel::class, AppLevelViewModel::class),
            actual = viewModels,
        )
        assertNull(catalog.findOrNull("NopeViewModel"))
    }

    @Test
    fun `a missing or repeated name fails`() {
        // Given
        val catalog = ViewModelCatalog(listOf(SampleViewModel::class, SampleViewModel::class))

        // When
        val missing = assertFailsWith<IllegalArgumentException> { catalog.find("NopeViewModel") }
        val repeated = assertFailsWith<IllegalArgumentException> { catalog.find("SampleViewModel") }

        // Then
        assertEquals(
            expected = listOf(
                "no ViewModel named NopeViewModel in the Koin graph",
                "SampleViewModel is ambiguous: [com.quare.bibleplanner.tools.agentcli.fake.SampleViewModel, " +
                    "com.quare.bibleplanner.tools.agentcli.fake.SampleViewModel]",
            ),
            actual = listOf(missing.message, repeated.message),
        )
    }
}
