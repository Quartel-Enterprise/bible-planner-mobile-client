package com.quare.bibleplanner.feature.releasenotes.presentation.factory

import com.quare.bibleplanner.core.date.DateRepresentation
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.domain.usecase.GetReleaseNotesUseCase
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ReleaseNotesUiStateFactoryTest {
    private lateinit var factory: ReleaseNotesUiStateFactory

    private val upcomingNote = ReleaseNoteModel(
        version = "999.0.0",
        changes = listOf("Something new"),
    )
    private val latestNote = ReleaseNoteModel(
        version = "0.0.3",
        changes = listOf("Latest change"),
        dateRepresentation = DateRepresentation.Yesterday,
    )
    private val unpublishedNote = ReleaseNoteModel(
        version = "0.0.2",
        changes = listOf("Never published"),
    )
    private val pastNote = ReleaseNoteModel(
        version = "0.0.1",
        changes = listOf("First change"),
        dateRepresentation = DateRepresentation.LastMonth,
    )

    @Test
    fun `GIVEN the loading screen WHEN creating the initial state THEN starts loading`() {
        // Given
        prepareScenario(result = Result.success(emptyList()))

        // When
        val state = factory.createInitialState()

        // Then
        assertEquals(ReleaseNotesUiState.Loading, state)
    }

    @Test
    fun `GIVEN upcoming and published notes WHEN creating the state THEN splits them into the three tabs`() = runTest {
        // Given
        prepareScenario(result = Result.success(listOf(upcomingNote, latestNote, unpublishedNote, pastNote)))

        // When
        val state = factory.create()

        // Then
        assertEquals(
            ReleaseNotesUiState.Success(
                latestRelease = latestNote,
                upcomingReleases = listOf(upcomingNote),
                pastReleases = listOf(pastNote),
                currentTab = ReleaseNotesTab.LATEST,
                availableTabs = listOf(
                    ReleaseNotesTab.PAST_VERSIONS,
                    ReleaseNotesTab.LATEST,
                    ReleaseNotesTab.UPCOMING,
                ),
            ),
            state,
        )
    }

    @Test
    fun `GIVEN no upcoming notes WHEN creating the state THEN hides the upcoming tab`() = runTest {
        // Given
        prepareScenario(result = Result.success(listOf(latestNote, pastNote)))

        // When
        val state = factory.create()

        // Then
        assertEquals(
            listOf(ReleaseNotesTab.PAST_VERSIONS, ReleaseNotesTab.LATEST),
            assertIs<ReleaseNotesUiState.Success>(state).availableTabs,
        )
    }

    @Test
    fun `GIVEN the notes fail to load WHEN creating the state THEN shows the error state`() = runTest {
        // Given
        prepareScenario(result = Result.failure(IllegalStateException("boom")))

        // When
        val state = factory.create()

        // Then
        assertEquals(ReleaseNotesUiState.Error, state)
    }

    private fun prepareScenario(result: Result<List<ReleaseNoteModel>>) {
        factory = ReleaseNotesUiStateFactory(GetReleaseNotesUseCase { result })
    }
}
