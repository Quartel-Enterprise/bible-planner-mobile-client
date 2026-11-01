package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_latest
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_past
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_upcoming
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseNotesTabResourceMapperTest {
    @Test
    fun `GIVEN the latest tab WHEN mapping its title THEN uses the latest update title`() {
        // Given
        val tab = ReleaseNotesTab.LATEST

        // When
        val title = tab.toTitleResource()

        // Then
        assertEquals(
            expected = Res.string.release_notes_tab_latest,
            actual = title,
        )
    }

    @Test
    fun `GIVEN the upcoming tab WHEN mapping its title THEN uses the future versions title`() {
        // Given
        val tab = ReleaseNotesTab.UPCOMING

        // When
        val title = tab.toTitleResource()

        // Then
        assertEquals(
            expected = Res.string.release_notes_tab_upcoming,
            actual = title,
        )
    }

    @Test
    fun `GIVEN the past versions tab WHEN mapping its title THEN uses the past versions title`() {
        // Given
        val tab = ReleaseNotesTab.PAST_VERSIONS

        // When
        val title = tab.toTitleResource()

        // Then
        assertEquals(
            expected = Res.string.release_notes_tab_past,
            actual = title,
        )
    }
}
