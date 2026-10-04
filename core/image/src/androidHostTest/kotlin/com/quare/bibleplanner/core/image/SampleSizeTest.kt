package com.quare.bibleplanner.core.image

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleSizeTest {
    @Test
    fun `GIVEN an image already near the target size WHEN calculating the sample size THEN does not subsample it`() {
        // Given
        val width = 600
        val height = 800

        // When
        val sampleSize = calculateSampleSize(
            width = width,
            height = height,
            targetPx = 512,
        )

        // Then
        assertEquals(1, sampleSize)
    }

    @Test
    fun `GIVEN a large image WHEN calculating the sample size THEN halves it while staying above the target`() {
        // Given
        val width = 4032
        val height = 3024

        // When
        val sampleSize = calculateSampleSize(
            width = width,
            height = height,
            targetPx = 512,
        )

        // Then
        assertEquals(4, sampleSize)
    }

    @Test
    fun `GIVEN an image smaller than the target WHEN calculating the sample size THEN does not subsample it`() {
        // Given
        val width = 100
        val height = 100

        // When
        val sampleSize = calculateSampleSize(
            width = width,
            height = height,
            targetPx = 512,
        )

        // Then
        assertEquals(1, sampleSize)
    }

    @Test
    fun `GIVEN invalid bounds WHEN calculating the sample size THEN falls back to no subsampling`() {
        // Given
        val width = 0
        val height = 0

        // When
        val sampleSize = calculateSampleSize(
            width = width,
            height = height,
            targetPx = 512,
        )

        // Then
        assertEquals(1, sampleSize)
    }
}
