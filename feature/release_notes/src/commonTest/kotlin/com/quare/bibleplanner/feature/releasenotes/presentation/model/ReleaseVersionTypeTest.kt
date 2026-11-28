package com.quare.bibleplanner.feature.releasenotes.presentation.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseVersionTypeTest {
    @Test
    fun `GIVEN every release type WHEN checking which can be tapped THEN only the upcoming one cannot`() {
        // Given
        val types = ReleaseVersionType.entries

        // When
        val clickableTypes = types.filter { it.isClickable }

        // Then
        assertEquals(
            expected = listOf(
                ReleaseVersionType.LATEST,
                ReleaseVersionType.PAST,
            ),
            actual = clickableTypes,
        )
    }
}
