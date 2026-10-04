package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane
import com.quare.bibleplanner.core.navigation.scene.SheetScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class SheetSceneStrategyTest {
    private val homeEntry = entry(key = MainNavRoute)
    private val themeSheetEntry = entry(
        key = ThemeNavRoute,
        metadata = getSheetPane(),
    )
    private val appearanceSheetEntry = entry(
        key = ReaderAppearanceNavRoute,
        metadata = getSheetPane(),
    )
    private val strategy = SheetSceneStrategy()

    @Test
    fun `GIVEN a sheet on top WHEN calculating THEN overlays it on the entries below`() {
        // When
        val scene = calculate(listOf(homeEntry, themeSheetEntry))

        // Then
        assertIs<SheetScene>(scene)
        assertEquals(themeSheetEntry.contentKey, scene.key)
        assertEquals(listOf(themeSheetEntry), scene.entries)
        assertEquals(listOf(homeEntry), scene.previousEntries)
        assertEquals(listOf(homeEntry), scene.overlaidEntries)
    }

    @Test
    fun `GIVEN a top entry that is not a sheet WHEN calculating THEN has no sheet`() {
        // When
        val scene = calculate(listOf(themeSheetEntry, homeEntry))

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN no entries WHEN calculating THEN has no sheet`() {
        // When
        val scene = calculate(emptyList())

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN the same entries WHEN calculating twice THEN builds equal scenes`() {
        // Given
        val first = calculate(listOf(homeEntry, themeSheetEntry))

        // When
        val second = calculate(listOf(homeEntry, themeSheetEntry))

        // Then
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertEquals(first.toString(), second.toString())
    }

    @Test
    fun `GIVEN another sheet on top WHEN calculating THEN builds a different scene`() {
        // Given
        val themeSheet = calculate(listOf(homeEntry, themeSheetEntry))

        // When
        val appearanceSheet = calculate(listOf(homeEntry, appearanceSheetEntry))

        // Then
        assertNotEquals(themeSheet, appearanceSheet)
        assertNotEquals<Any?>(themeSheet, homeEntry)
    }

    private fun calculate(entries: List<NavEntry<NavKey>>): Scene<NavKey>? =
        with(strategy) { SceneStrategyScope<NavKey>().calculateScene(entries) }

    private fun entry(
        key: NavKey,
        metadata: Map<String, Any> = emptyMap(),
    ): NavEntry<NavKey> = NavEntry(
        key = key,
        metadata = metadata,
        content = {},
    )
}
