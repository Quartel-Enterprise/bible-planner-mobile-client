package com.quare.bibleplanner.feature.bibleversion.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermissionUseCase
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DismissBibleUpdatePromptUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPendingBibleUpdatesUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.UpdateBibleVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.NoOpDeleteVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingDownloadNotifier
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingNotificationPermissionRequester
import com.quare.bibleplanner.feature.bibleversion.fake.bibleModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdateItem
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
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
internal class PendingBibleUpdatesViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PendingBibleUpdatesViewModel
    private lateinit var downloaderFacade: FakeBibleVersionDownloaderFacade
    private lateinit var permissionRequester: RecordingNotificationPermissionRequester
    private lateinit var promptPreferences: RecordingPromptPreferences
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var navigator: Navigator

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        downloaderFacade = FakeBibleVersionDownloaderFacade()
        permissionRequester = RecordingNotificationPermissionRequester()
        promptPreferences = RecordingPromptPreferences()
        trackedEvents = mutableListOf()
        navigator = Navigator()
        val bibleVersionDao = InMemoryBibleVersionDao(
            listOf(FIRST_ID, SECOND_ID).map { id ->
                BibleVersionEntity(
                    id = id,
                    status = DownloadStatus.DONE,
                )
            },
        )
        viewModel = PendingBibleUpdatesViewModel(
            updateBibleVersion = UpdateBibleVersionUseCase(
                deleteBibleVersionDownload = DeleteBibleVersionDownloadUseCase(
                    bibleVersionDao = bibleVersionDao,
                    verseDao = NoOpDeleteVerseDao(),
                    notifier = RecordingDownloadNotifier(),
                ),
                downloaderFacade = downloaderFacade,
            ),
            dismissBibleUpdatePrompt = DismissBibleUpdatePromptUseCase(
                bibleUpdatePromptPreferences = promptPreferences,
                currentTimestampProvider = { NOW },
            ),
            requestDownloadNotificationPermission = RequestDownloadNotificationPermissionUseCase(
                notificationPermissionRequester = permissionRequester,
                trackEvent = { _, _ -> },
                navigator = navigator,
            ),
            navigator = navigator,
            getPendingBibleUpdates = GetPendingBibleUpdatesUseCase(
                FakeBibleRepository(
                    listOf(
                        bibleModel(
                            id = FIRST_ID,
                            hasPendingUpdate = true,
                        ),
                        bibleModel(id = "UP_TO_DATE"),
                        bibleModel(
                            id = SECOND_ID,
                            hasPendingUpdate = true,
                        ),
                    ),
                ),
            ),
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `lists every version with a pending update selected`() = runTest(testDispatcher) {
        // When
        val pendingUpdates = viewModel.pendingUpdates.value

        // Then
        assertEquals(
            expected = listOf(
                PendingBibleUpdateItem(
                    id = FIRST_ID,
                    name = "Version $FIRST_ID",
                    size = 8_000_000L,
                    isSelected = true,
                ),
                PendingBibleUpdateItem(
                    id = SECOND_ID,
                    name = "Version $SECOND_ID",
                    size = 8_000_000L,
                    isSelected = true,
                ),
            ),
            actual = pendingUpdates,
        )
    }

    @Test
    fun `toggling a version unselects it and tracks its new state`() = runTest(testDispatcher) {
        // When
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnToggleVersion(FIRST_ID))

        // Then
        assertEquals(
            expected = listOf(false, true),
            actual = viewModel.pendingUpdates.value.map { it.isSelected },
        )
        assertEquals(
            expected = listOf(
                AnalyticsEventNames.BIBLE_VERSION_UPDATE_PROMPT_VERSION_TOGGLED to mapOf<String, Any>(
                    AnalyticsParams.VERSION_ID to FIRST_ID,
                    AnalyticsParams.IS_SELECTED to false,
                ),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `updating downloads the selected versions asks for the permission and closes`() = runTest(testDispatcher) {
        // Given
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnToggleVersion(FIRST_ID))

        // When
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnUpdateClick)

        // Then
        assertEquals(
            expected = listOf("download $SECOND_ID"),
            actual = downloaderFacade.calls,
        )
        assertEquals(
            expected = 1,
            actual = permissionRequester.requestCount,
        )
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = navigationCommands(),
        )
    }

    @Test
    fun `updating with nothing selected only closes`() = runTest(testDispatcher) {
        // Given
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnToggleVersion(FIRST_ID))
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnToggleVersion(SECOND_ID))

        // When
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnUpdateClick)

        // Then
        assertTrue(downloaderFacade.calls.isEmpty())
        assertEquals(
            expected = 0,
            actual = permissionRequester.requestCount,
        )
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = navigationCommands(),
        )
    }

    @Test
    fun `dismissing remembers the dismissal and closes`() = runTest(testDispatcher) {
        // When
        viewModel.onEvent(PendingBibleUpdatesUiEvent.OnDismissClick)

        // Then
        assertEquals(
            expected = listOf(NOW),
            actual = promptPreferences.dismissals,
        )
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = navigationCommands(),
        )
        assertEquals(
            expected = listOf(AnalyticsEventNames.BIBLE_VERSION_UPDATE_PROMPT_DISMISSED),
            actual = trackedEvents.map { it.first },
        )
    }

    private fun TestScope.navigationCommands(): List<NavigationCommand> =
        mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }

    private companion object {
        const val FIRST_ID = "ACF"
        const val SECOND_ID = "KJV"
        const val NOW = 1_700_000_000_000L
    }
}

private class RecordingPromptPreferences : BibleUpdatePromptPreferences {
    val dismissals = mutableListOf<Long>()

    override suspend fun getLastDismissedAt(): Long? = dismissals.lastOrNull()

    override suspend fun setLastDismissedAt(timestamp: Long) {
        dismissals += timestamp
    }
}
