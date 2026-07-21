package com.quare.bibleplanner.core.image

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleSizeTest {
    @Test
    fun `does not subsample an image already near the target size`() {
        // When
        val sampleSize = calculateSampleSize(
            width = 600,
            height = 800,
            targetPx = 512,
        )

        // Then
        assertEquals(1, sampleSize)
    }

    @Test
    fun `halves a large image until the shorter side is still above the target`() {
        // When
        val sampleSize = calculateSampleSize(
            width = 4032,
            height = 3024,
            targetPx = 512,
        )

        // Then
        assertEquals(4, sampleSize)
    }

    @Test
    fun `does not subsample an image smaller than the target`() {
        // When
        val sampleSize = calculateSampleSize(
            width = 100,
            height = 100,
            targetPx = 512,
        )

        // Then
        assertEquals(1, sampleSize)
    }

    @Test
    fun `falls back to no subsampling for invalid bounds`() {
        // When
        val sampleSize = calculateSampleSize(
            width = 0,
            height = 0,
            targetPx = 512,
        )

        // Then
        assertEquals(1, sampleSize)
    }
}
