package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.getDayStudyDetailPane
import com.quare.bibleplanner.core.model.route.getDayStudyMainPane
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute
import com.quare.bibleplanner.core.navigation.scene.DayStudyPanelScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class DayStudyPanelSceneStrategyTest {
    private val dayRoute = DayNavRoute(
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = "CHRONOLOGICAL",
    )
    private val homeEntry = entry(key = MainNavRoute)
    private val dayEntry = entry(
        key = dayRoute,
        metadata = getDayStudyMainPane(),
    )
    private val studyEntry = entry(
        key = dayRoute.toDayStudyNavRoute(),
        metadata = getDayStudyDetailPane(),
    )

    private lateinit var strategy: DayStudyPanelSceneStrategy

    @Test
    fun `GIVEN a wide window with a day and its study on top WHEN calculating THEN shows them side by side`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val scene = calculate(listOf(homeEntry, dayEntry, studyEntry))

        // Then
        assertIs<DayStudyPanelScene>(scene)
        assertEquals(dayEntry.contentKey, scene.key)
        assertEquals(listOf(dayEntry, studyEntry), scene.entries)
        assertEquals(listOf(homeEntry), scene.previousEntries)
    }

    @Test
    fun `GIVEN a narrow window WHEN calculating THEN leaves the entries to the next strategy`() {
        // Given
        prepareScenario(isWide = false)

        // When
        val scene = calculate(listOf(homeEntry, dayEntry, studyEntry))

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN fewer than two entries WHEN calculating THEN has no panel`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val fromEmpty = calculate(emptyList())
        val fromSingle = calculate(listOf(studyEntry))

        // Then
        assertNull(fromEmpty)
        assertNull(fromSingle)
    }

    @Test
    fun `GIVEN a top entry that is not the study pane WHEN calculating THEN has no panel`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val scene = calculate(listOf(dayEntry, entry(key = ThemeNavRoute)))

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN a study pane over an entry that is not the day WHEN calculating THEN has no panel`() {
        // Given
        prepareScenario(isWide = true)

        // When
        val scene = calculate(listOf(homeEntry, studyEntry))

        // Then
        assertNull(scene)
    }

    @Test
    fun `GIVEN the same entries WHEN calculating twice with other ratios THEN keeps the same scene`() {
        // Given
        prepareScenario(isWide = true)
        val first = calculate(listOf(homeEntry, dayEntry, studyEntry))
        prepareScenario(
            isWide = true,
            readingFraction = 0.7f,
        )

        // When
        val second = calculate(listOf(homeEntry, dayEntry, studyEntry))

        // Then
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertEquals(first.toString(), second.toString())
    }

    @Test
    fun `GIVEN different previous entries WHEN calculating THEN builds a different scene`() {
        // Given
        prepareScenario(isWide = true)
        val withHome = calculate(listOf(homeEntry, dayEntry, studyEntry))

        // When
        val withoutHome = calculate(listOf(dayEntry, studyEntry))

        // Then
        assertNotEquals(withHome, withoutHome)
        assertNotEquals<Any?>(withHome, dayEntry)
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

    private fun prepareScenario(
        isWide: Boolean,
        readingFraction: Float = 0.5f,
    ) {
        strategy = DayStudyPanelSceneStrategy(
            isWide = isWide,
            readingFraction = readingFraction,
            onReadingFractionCommit = {},
        )
    }
}
