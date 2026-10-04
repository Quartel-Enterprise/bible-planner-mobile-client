package com.quare.bibleplanner.ui.utils.sheet

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SheetExitAnimationTest {
    private lateinit var sheetExitAnimation: SheetExitAnimation

    @BeforeTest
    fun setUp() {
        sheetExitAnimation = SheetExitAnimation()
    }

    @Test
    fun `GIVEN a sheet that has not been popped WHEN reading it THEN is not exiting`() {
        // When
        val isExiting = sheetExitAnimation.isExiting

        // Then
        assertFalse(isExiting)
    }

    @Test
    fun `GIVEN no registered animation WHEN playing THEN is exiting and returns at once`() = runTest {
        // When
        sheetExitAnimation.play()

        // Then
        assertTrue(sheetExitAnimation.isExiting)
    }

    @Test
    fun `GIVEN a registered animation WHEN playing THEN returns only once it ends`() = runTest {
        // Given
        val animationEnd = CompletableDeferred<Unit>()
        sheetExitAnimation.register { animationEnd.await() }

        // When
        val playing = async { sheetExitAnimation.play() }
        testScheduler.advanceUntilIdle()
        val isDoneBeforeAnimationEnds = playing.isCompleted
        animationEnd.complete(Unit)
        playing.await()

        // Then
        assertFalse(isDoneBeforeAnimationEnds)
        assertTrue(sheetExitAnimation.isExiting)
    }

    @Test
    fun `GIVEN the registered animation is unregistered WHEN playing THEN does not run it`() = runTest {
        // Given
        var runs = 0
        val animation: suspend () -> Unit = { runs++ }
        sheetExitAnimation.register(animation)
        sheetExitAnimation.unregister(animation)

        // When
        sheetExitAnimation.play()

        // Then
        assertEquals(0, runs)
    }

    @Test
    fun `GIVEN another animation is unregistered WHEN playing THEN still runs the registered one`() = runTest {
        // Given
        var runs = 0
        sheetExitAnimation.register { runs++ }
        sheetExitAnimation.unregister {}

        // When
        sheetExitAnimation.play()

        // Then
        assertEquals(1, runs)
    }

    @Test
    fun `GIVEN the animation is interrupted WHEN playing THEN returns so the sheet can leave`() = runTest {
        // Given
        sheetExitAnimation.register { throw CancellationException("interrupted by another animation") }

        // When
        sheetExitAnimation.play()

        // Then
        assertTrue(sheetExitAnimation.isExiting)
    }

    @Test
    fun `GIVEN the exit itself is cancelled WHEN playing THEN stops and is no longer exiting`() = runTest {
        // Given
        val animationStart = CompletableDeferred<Unit>()
        sheetExitAnimation.register {
            animationStart.complete(Unit)
            CompletableDeferred<Unit>().await()
        }

        // When
        val playing = async { sheetExitAnimation.play() }
        animationStart.await()
        playing.cancel()
        testScheduler.advanceUntilIdle()

        // Then
        assertTrue(playing.isCancelled)
        assertFalse(sheetExitAnimation.isExiting)
    }
}
