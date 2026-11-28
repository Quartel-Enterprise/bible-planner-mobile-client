package com.quare.bibleplanner.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VisibleBarClearanceTest {
    @Test
    fun `GIVEN the bar is partly hidden WHEN getting the clearance THEN it is the visible part of the bar`() {
        // Given
        val heightOffsetPx = -30f

        // When
        val clearance = getVisibleBarClearancePx(
            barHeightPx = BAR_HEIGHT_PX,
            heightOffsetPx = heightOffsetPx,
            navigationBarBottomPx = NAVIGATION_BAR_BOTTOM_PX,
        )

        // Then
        assertEquals(170f, clearance)
    }

    @Test
    fun `GIVEN the bar is fully hidden WHEN getting the clearance THEN it is the gesture bar inset`() {
        // Given
        val heightOffsetPx = -BAR_HEIGHT_PX

        // When
        val clearance = getVisibleBarClearancePx(
            barHeightPx = BAR_HEIGHT_PX,
            heightOffsetPx = heightOffsetPx,
            navigationBarBottomPx = NAVIGATION_BAR_BOTTOM_PX,
        )

        // Then
        assertEquals(NAVIGATION_BAR_BOTTOM_PX, clearance)
    }

    companion object {
        private const val BAR_HEIGHT_PX = 200f
        private const val NAVIGATION_BAR_BOTTOM_PX = 48f
    }
}
