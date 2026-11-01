package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseVersionTypeTagColorsTest {
    @Test
    fun `GIVEN the upcoming release type WHEN checking if it is upcoming THEN returns true`() {
        // Given
        val type = ReleaseVersionType.UPCOMING

        // When
        val isUpcoming = type.isUpcoming()

        // Then
        assertEquals(
            expected = true,
            actual = isUpcoming,
        )
    }

    @Test
    fun `GIVEN the latest and past release types WHEN checking if they are upcoming THEN returns false`() {
        // Given
        val types = listOf(
            ReleaseVersionType.LATEST,
            ReleaseVersionType.PAST,
        )

        // When
        val upcomingFlags = types.map { it.isUpcoming() }

        // Then
        assertEquals(
            expected = listOf(false, false),
            actual = upcomingFlags,
        )
    }
}
