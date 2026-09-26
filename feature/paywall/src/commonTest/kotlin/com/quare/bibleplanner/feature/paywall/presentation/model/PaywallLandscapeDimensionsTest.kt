package com.quare.bibleplanner.feature.paywall.presentation.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PaywallLandscapeDimensionsTest {
    @Test
    fun `GIVEN a phone in landscape WHEN reading the compact dimensions THEN opens the panel without the hero icon`() {
        // When
        val dimensions = PaywallLandscapeDimensions.Compact

        // Then
        assertNull(dimensions.heroIcon)
        assertFalse(dimensions.centersPlanBlock)
    }

    @Test
    fun `GIVEN a large landscape screen WHEN reading the regular dimensions THEN shows the hero icon`() {
        // When
        val dimensions = PaywallLandscapeDimensions.Regular

        // Then
        val heroIcon = assertNotNull(dimensions.heroIcon)
        assertTrue(heroIcon.iconSize < heroIcon.boxSize)
        assertTrue(dimensions.centersPlanBlock)
    }

    @Test
    fun `GIVEN both landscape layouts WHEN comparing them THEN the compact one is denser than the regular one`() {
        // Given
        val compact = PaywallLandscapeDimensions.Compact

        // When
        val regular = PaywallLandscapeDimensions.Regular

        // Then
        assertTrue(compact.heroTitleFontSize.value < regular.heroTitleFontSize.value)
        assertTrue(compact.heroBottomSpacing < regular.heroBottomSpacing)
        assertTrue(compact.featureSpacing < regular.featureSpacing)
        assertTrue(compact.planPriceFontSize.value < regular.planPriceFontSize.value)
        assertTrue(compact.actionButtonHeight < regular.actionButtonHeight)
    }
}
