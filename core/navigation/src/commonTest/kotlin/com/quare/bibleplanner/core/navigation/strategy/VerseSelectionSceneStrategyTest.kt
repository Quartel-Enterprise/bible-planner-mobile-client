package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.model.route.getReaderPane
import com.quare.bibleplanner.core.model.route.getVerseSelectionPane
import com.quare.bibleplanner.core.navigation.scene.VerseSelectionScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class VerseSelectionSceneStrategyTest {
    private val homeEntry = entry(key = MainNavRoute)
    private val readerEntry = entry(
        key = ReadNavRoute(
            bookId = "JHN",
            chapterNumber = 3,
            isChapterRead = false,
            isFromBookDetails = false,
        ),
        metadata = getReaderPane(),
    )
    private val selectionEntry = entry(
        key = VerseSelectionNavRoute,
        metadata = getVerseSelectionPane(),
    )

    private lateinit var strategy: VerseSelectionSceneStrategy

    @Test
    fun `GIVEN a selection panel over the reader WHEN calculating THEN keeps the reader under the panel`() {
        // Given
        prepareScenario(isWide = false)

        // When
        val scene = calculate(listOf(homeEntry, readerEntry, selectionEntry))

        // Then
        assertIs<VerseSelectionScene>(scene)
        assertEquals(readerEntry.contentKey, scene.key)
        assertEquals(listOf(readerEntry, selectionEntry), scene.entries)
        assertEquals(listOf(homeEntry, readerEntry), scene.previousEntries)
    }

    @Test
    fun `GIVEN fewer than two entries WHEN calculating THEN has no panel`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val fromEmpty = calculate(emptyList())
        val fromSingle = calculate(listOf(selectionEntry))

        // Then
        assertNull(fromEmpty)
        assertNull(fromSingle)
    }

    @Test
    fun `GIVEN a top entry that is not the selection panel WHEN calculating THEN has no panel`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val scene = calculate(listOf(readerEntry, entry(key = ThemeNavRoute)))

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN a selection panel over an entry that is not the reader WHEN calculating THEN has no panel`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val scene = calculate(listOf(homeEntry, selectionEntry))

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN the same entries WHEN calculating twice THEN builds equal scenes`() {
        // Given
        prepareScenario(isWide = true)
        val first = calculate(listOf(readerEntry, selectionEntry))

        // When
        val second = calculate(listOf(readerEntry, selectionEntry))

        // Then
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertEquals(first.toString(), second.toString())
    }

    @Test
    fun `GIVEN the window width changes WHEN calculating THEN builds a different scene`() {
        // Given
        prepareScenario(isWide = true)
        val wide = calculate(listOf(readerEntry, selectionEntry))
        prepareScenario(isWide = false)

        // When
        val narrow = calculate(listOf(readerEntry, selectionEntry))

        // Then
        assertNotEquals(wide, narrow)
        assertNotEquals<Any?>(wide, readerEntry)
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

    private fun prepareScenario(isWide: Boolean) {
        strategy = VerseSelectionSceneStrategy(isWide = isWide)
    }
}
