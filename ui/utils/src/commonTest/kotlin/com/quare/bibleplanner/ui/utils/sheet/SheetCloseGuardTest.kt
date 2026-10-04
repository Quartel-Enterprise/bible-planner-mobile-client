package com.quare.bibleplanner.ui.utils.sheet

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SheetCloseGuardTest {
    private var closes = 0
    private val sheetExitAnimation = SheetExitAnimation()
    private val sheetCloseGuard = SheetCloseGuard(sheetExitAnimation)

    @Test
    fun `GIVEN an open sheet WHEN closing twice before its route is popped THEN closes once`() {
        // When
        sheetCloseGuard.close { closes++ }
        sheetCloseGuard.close { closes++ }

        // Then
        assertEquals(1, closes)
        assertTrue(sheetCloseGuard.isClosing)
    }

    @Test
    fun `GIVEN a sheet animating away WHEN closing THEN ignores it`() = runTest {
        // Given
        val animationEnd = CompletableDeferred<Unit>()
        sheetExitAnimation.register { animationEnd.await() }
        val exit = async { sheetExitAnimation.play() }
        testScheduler.advanceUntilIdle()

        // When
        sheetCloseGuard.close { closes++ }
        animationEnd.complete(Unit)
        exit.await()

        // Then
        assertEquals(0, closes)
        assertTrue(sheetCloseGuard.isClosing)
    }

    @Test
    fun `GIVEN a close the screen never answered WHEN it expires THEN accepts the next close`() = runTest {
        // Given
        sheetCloseGuard.close { closes++ }

        // When
        sheetCloseGuard.expirePendingClose()
        sheetCloseGuard.close { closes++ }

        // Then
        assertEquals(2, closes)
    }

    @Test
    fun `GIVEN a close answered by popping the route WHEN it expires THEN still refuses closes while leaving`() =
        runTest {
            // Given
            sheetExitAnimation.register { CompletableDeferred<Unit>().await() }
            sheetCloseGuard.close { closes++ }
            val exit = async { sheetExitAnimation.play() }
            testScheduler.advanceUntilIdle()

            // When
            sheetCloseGuard.expirePendingClose()
            val isClosing = sheetCloseGuard.isClosing
            exit.cancel()

            // Then
            assertTrue(isClosing)
            assertFalse(sheetCloseGuard.hasPendingClose)
        }

    @Test
    fun `GIVEN a sheet outside a sheet scene WHEN closing THEN closes and is pending`() {
        // Given
        val guardWithoutScene = SheetCloseGuard(sheetExitAnimation = null)

        // When
        guardWithoutScene.close { closes++ }

        // Then
        assertEquals(1, closes)
        assertTrue(guardWithoutScene.isClosing)
    }
}
