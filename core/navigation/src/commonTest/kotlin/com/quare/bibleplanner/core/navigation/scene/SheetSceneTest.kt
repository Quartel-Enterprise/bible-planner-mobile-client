package com.quare.bibleplanner.core.navigation.scene

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SheetSceneTest {
    private val homeEntry = entry(key = MainNavRoute)
    private val sheetEntry = entry(key = ThemeNavRoute)
    private lateinit var exitAnimation: SheetExitAnimation
    private lateinit var scene: SheetScene

    @BeforeTest
    fun setUp() {
        exitAnimation = SheetExitAnimation()
        scene = SheetScene(
            key = sheetEntry.contentKey,
            entry = sheetEntry,
            previousEntries = listOf(homeEntry),
            exitAnimation = exitAnimation,
        )
    }

    @Test
    fun `GIVEN a popped sheet animating away WHEN removing it THEN waits until the animation ends`() = runTest {
        // Given
        val animationEnd = CompletableDeferred<Unit>()
        exitAnimation.register { animationEnd.await() }

        // When
        val removal = async { scene.onRemove() }
        testScheduler.advanceUntilIdle()
        val isRemovedBeforeAnimationEnds = removal.isCompleted
        animationEnd.complete(Unit)
        removal.await()

        // Then
        assertFalse(isRemovedBeforeAnimationEnds)
        assertTrue(exitAnimation.isExiting)
    }

    @Test
    fun `GIVEN a sheet with no exit animation WHEN removing it THEN leaves at once`() = runTest {
        // When
        scene.onRemove()

        // Then
        assertTrue(exitAnimation.isExiting)
    }

    @Test
    fun `GIVEN two scenes with their own exit animations WHEN comparing THEN only the entries matter`() {
        // Given
        val sameEntries = SheetScene(
            key = sheetEntry.contentKey,
            entry = sheetEntry,
            previousEntries = listOf(homeEntry),
            exitAnimation = SheetExitAnimation(),
        )

        // When
        val isEqual = scene == sameEntries

        // Then
        assertTrue(isEqual)
        assertEquals(scene.hashCode(), sameEntries.hashCode())
        assertEquals(listOf(homeEntry), scene.overlaidEntries)
    }

    private fun entry(key: NavKey): NavEntry<NavKey> = NavEntry(
        key = key,
        content = {},
    )
}
