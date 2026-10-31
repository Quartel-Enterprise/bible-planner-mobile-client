package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tag_upcoming
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ReleaseVersionTypeResourceMapperTest {
    @Test
    fun `GIVEN the latest release type WHEN mapping its tag THEN uses the latest tag`() {
        // Given
        val type = ReleaseVersionType.LATEST

        // When
        val tag = type.toTagResource()

        // Then
        assertEquals(
            expected = Res.string.release_notes_tag_latest,
            actual = tag,
        )
    }

    @Test
    fun `GIVEN the upcoming release type WHEN mapping its tag THEN uses the upcoming tag`() {
        // Given
        val type = ReleaseVersionType.UPCOMING

        // When
        val tag = type.toTagResource()

        // Then
        assertEquals(
            expected = Res.string.release_notes_tag_upcoming,
            actual = tag,
        )
    }

    @Test
    fun `GIVEN the past release type WHEN mapping its tag THEN shows no tag`() {
        // Given
        val type = ReleaseVersionType.PAST

        // When
        val tag = type.toTagResource()

        // Then
        assertNull(tag)
    }
}
