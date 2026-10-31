package com.quare.bibleplanner.feature.releasenotes.presentation.utils

import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseVersionType
import com.quare.bibleplanner.feature.releasenotes.presentation.model.VisibleReleaseNote
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseNotesVisibleNotesMapperTest {
    private lateinit var state: ReleaseNotesUiState.Success

    private val firstUpcomingNote = ReleaseNoteModel(
        version = "999.1.0",
        changes = listOf("Coming later"),
        dateRepresentation = null,
    )
    private val secondUpcomingNote = ReleaseNoteModel(
        version = "999.0.0",
        changes = listOf("Coming soon"),
        dateRepresentation = null,
    )
    private val latestNote = ReleaseNoteModel(
        version = "0.0.3",
        changes = listOf("Latest change"),
        dateRepresentation = DateRepresentation.Yesterday,
    )
    private val firstPastNote = ReleaseNoteModel(
        version = "0.0.2",
        changes = listOf("Second change"),
        dateRepresentation = DateRepresentation.LastMonth,
    )
    private val secondPastNote = ReleaseNoteModel(
        version = "0.0.1",
        changes = listOf("First change"),
        dateRepresentation = DateRepresentation.LastMonth,
    )

    @Test
    fun `GIVEN the latest tab WHEN mapping the visible notes THEN shows only the latest release tagged as latest`() {
        // Given
        prepareScenario(currentTab = ReleaseNotesTab.LATEST)

        // When
        val visibleNotes = state.toVisibleNotes()

        // Then
        assertEquals(
            expected = listOf(
                VisibleReleaseNote(
                    note = latestNote,
                    type = ReleaseVersionType.LATEST,
                ),
            ),
            actual = visibleNotes,
        )
    }

    @Test
    fun `GIVEN the latest tab without a latest release WHEN mapping the visible notes THEN shows no notes`() {
        // Given
        prepareScenario(
            currentTab = ReleaseNotesTab.LATEST,
            latestRelease = null,
        )

        // When
        val visibleNotes = state.toVisibleNotes()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = visibleNotes,
        )
    }

    @Test
    fun `GIVEN the upcoming tab WHEN mapping the visible notes THEN shows the upcoming releases in order`() {
        // Given
        prepareScenario(currentTab = ReleaseNotesTab.UPCOMING)

        // When
        val visibleNotes = state.toVisibleNotes()

        // Then
        assertEquals(
            expected = listOf(
                VisibleReleaseNote(
                    note = firstUpcomingNote,
                    type = ReleaseVersionType.UPCOMING,
                ),
                VisibleReleaseNote(
                    note = secondUpcomingNote,
                    type = ReleaseVersionType.UPCOMING,
                ),
            ),
            actual = visibleNotes,
        )
    }

    @Test
    fun `GIVEN the upcoming tab without upcoming releases WHEN mapping the visible notes THEN shows no notes`() {
        // Given
        prepareScenario(
            currentTab = ReleaseNotesTab.UPCOMING,
            upcomingReleases = emptyList(),
        )

        // When
        val visibleNotes = state.toVisibleNotes()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = visibleNotes,
        )
    }

    @Test
    fun `GIVEN the past versions tab WHEN mapping the visible notes THEN shows the past releases in order`() {
        // Given
        prepareScenario(currentTab = ReleaseNotesTab.PAST_VERSIONS)

        // When
        val visibleNotes = state.toVisibleNotes()

        // Then
        assertEquals(
            expected = listOf(
                VisibleReleaseNote(
                    note = firstPastNote,
                    type = ReleaseVersionType.PAST,
                ),
                VisibleReleaseNote(
                    note = secondPastNote,
                    type = ReleaseVersionType.PAST,
                ),
            ),
            actual = visibleNotes,
        )
    }

    @Test
    fun `GIVEN the past versions tab without past releases WHEN mapping the visible notes THEN shows no notes`() {
        // Given
        prepareScenario(
            currentTab = ReleaseNotesTab.PAST_VERSIONS,
            pastReleases = emptyList(),
        )

        // When
        val visibleNotes = state.toVisibleNotes()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = visibleNotes,
        )
    }

    private fun prepareScenario(
        currentTab: ReleaseNotesTab,
        latestRelease: ReleaseNoteModel? = latestNote,
        upcomingReleases: List<ReleaseNoteModel> = listOf(firstUpcomingNote, secondUpcomingNote),
        pastReleases: List<ReleaseNoteModel> = listOf(firstPastNote, secondPastNote),
    ) {
        state = ReleaseNotesUiState.Success(
            latestRelease = latestRelease,
            upcomingReleases = upcomingReleases,
            pastReleases = pastReleases,
            currentTab = currentTab,
            availableTabs = ReleaseNotesTab.entries,
        )
    }
}
