package com.quare.bibleplanner.feature.releasenotes.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.feature.release_notes.generated.resources.Res
import bibleplanner.feature.release_notes.generated.resources.release_notes_error_loading
import bibleplanner.feature.release_notes.generated.resources.release_notes_screen_title
import bibleplanner.feature.release_notes.generated.resources.release_notes_tab_past
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesTab
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiEvent
import com.quare.bibleplanner.feature.releasenotes.presentation.model.ReleaseNotesUiState
import com.quare.bibleplanner.ui.testing.AnimationsDisabled
import com.quare.bibleplanner.ui.testing.SharedTransitionTestContent
import com.quare.bibleplanner.ui.testing.setUiTestContent
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class ReleaseNotesUiTest {
    private var isReleaseNotesShown by mutableStateOf(false)
    private lateinit var sentEvents: MutableList<ReleaseNotesUiEvent>
    private val successState = ReleaseNotesUiState.Success(
        latestRelease = null,
        upcomingReleases = emptyList(),
        pastReleases = emptyList(),
        currentTab = ReleaseNotesTab.LATEST,
        availableTabs = listOf(
            ReleaseNotesTab.LATEST,
            ReleaseNotesTab.PAST_VERSIONS,
        ),
    )

    @Test
    fun `GIVEN animations disabled WHEN opening the release notes THEN shows the title in the top bar`() =
        runComposeUiTest(effectContext = AnimationsDisabled) {
            // Given
            val title = getString(Res.string.release_notes_screen_title)
            prepareScenario(uiState = ReleaseNotesUiState.Error)

            // When
            isReleaseNotesShown = true
            waitForIdle()

            // Then
            val titleNode = onNodeWithText(title)
            titleNode.assertIsDisplayed()
            val titleBounds = titleNode.getUnclippedBoundsInRoot()
            val errorTop = onNodeWithText(getString(Res.string.release_notes_error_loading))
                .getUnclippedBoundsInRoot()
                .top
            assertTrue(
                actual = titleBounds.bottom <= errorTop,
                message = "Title at $titleBounds should sit above the content at $errorTop",
            )
        }

    @Test
    fun `GIVEN the latest tab is selected WHEN tapping another tab THEN sends the tab selection`() =
        runComposeUiTest(effectContext = AnimationsDisabled) {
            // Given
            prepareScenario(uiState = successState)
            isReleaseNotesShown = true
            waitForIdle()

            // When
            onNodeWithText(getString(Res.string.release_notes_tab_past)).performClick()

            // Then
            assertEquals(
                expected = listOf<ReleaseNotesUiEvent>(
                    ReleaseNotesUiEvent.OnTabSelected(ReleaseNotesTab.PAST_VERSIONS),
                ),
                actual = sentEvents,
            )
        }

    private suspend fun ComposeUiTest.prepareScenario(uiState: ReleaseNotesUiState) {
        val title = getString(Res.string.release_notes_screen_title)
        sentEvents = mutableListOf()
        setUiTestContent {
            SharedTransitionTestContent(
                isTargetShown = isReleaseNotesShown,
                source = { animatedContentScope ->
                    Text(
                        text = title,
                        modifier = Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = RELEASE_NOTES_KEY),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    )
                },
                target = { animatedContentScope ->
                    ReleaseNotesScreen(
                        platform = Platform.Android,
                        uiState = uiState,
                        onEvent = sentEvents::add,
                        sharedTransitionScope = this,
                        animatedContentScope = animatedContentScope,
                    )
                },
            )
        }
    }

    private companion object {
        const val RELEASE_NOTES_KEY = "release_notes_card"
    }
}
