package com.quare.bibleplanner.feature.deleteversion.presentation.viewmodel

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiEvent
import com.quare.bibleplanner.feature.deleteversion.presentation.model.DeleteVersionUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeleteVersionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DeleteVersionViewModel
    private lateinit var downloader: FakeBibleVersionDownloaderFacade
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a slow deletion WHEN confirming THEN shows the loading state until it finishes`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(DeleteVersionUiEvent.OnConfirmDelete)

            // Then
            assertEquals(DeleteVersionUiState.Loading, viewModel.uiState.value)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a downloaded version WHEN the deletion finishes THEN deletes it, tracks it and closes`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(DeleteVersionUiEvent.OnConfirmDelete)

            // When
            downloader.finishDeletion()

            // Then
            assertEquals(listOf(VERSION_ID), downloader.deletedVersions)
            assertEquals(
                listOf(
                    AnalyticsEventNames.BIBLE_VERSION_DELETED to
                        mapOf<String, Any>(AnalyticsParams.VERSION_ID to VERSION_ID),
                ),
                trackedEvents,
            )
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        }

    @Test
    fun `GIVEN the confirmation WHEN cancelling THEN closes without deleting`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(DeleteVersionUiEvent.OnCancel)

        // Then
        assertTrue(downloader.deletedVersions.isEmpty())
        assertEquals(DeleteVersionUiState.Idle, viewModel.uiState.value)
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(
            listOf(AnalyticsEventNames.BIBLE_VERSION_DELETE_CANCELLED),
            trackedEvents.map { (name, _) ->
                name
            },
        )
    }

    private fun TestScope.prepareScenario() {
        val navigator = Navigator()
        downloader = FakeBibleVersionDownloaderFacade()
        commands = mutableListOf()
        trackedEvents = mutableListOf()
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = DeleteVersionViewModel(
            bibleVersionDownloaderFacade = downloader,
            navigator = navigator,
            route = DeleteVersionNavRoute(versionId = VERSION_ID),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    private companion object {
        const val VERSION_ID = "NVI"
    }
}

private class FakeBibleVersionDownloaderFacade : BibleVersionDownloaderFacade {
    private val deletion = CompletableDeferred<Unit>()
    val deletedVersions = mutableListOf<String>()

    fun finishDeletion() {
        deletion.complete(Unit)
    }

    override suspend fun deleteDownload(versionId: String) {
        deletion.await()
        deletedVersions += versionId
    }

    override val shouldShowDownloadTip: Boolean get() = error("unused")

    override fun downloadVersion(versionId: String) = error("unused")

    override suspend fun pauseDownload(versionId: String) = error("unused")
}
