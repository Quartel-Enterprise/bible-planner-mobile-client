package com.quare.bibleplanner.feature.bibleversion.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermissionUseCase
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DeleteBibleVersionDownloadUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetBibleVersionsByLanguageUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.UpdateBibleVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.bibleversion.fake.FakeBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.bibleversion.fake.InMemoryBibleVersionDao
import com.quare.bibleplanner.feature.bibleversion.fake.NoOpDeleteVerseDao
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingDownloadNotifier
import com.quare.bibleplanner.feature.bibleversion.fake.RecordingNotificationPermissionRequester
import com.quare.bibleplanner.feature.bibleversion.fake.bibleModel
import com.quare.bibleplanner.feature.bibleversion.presentation.factory.BibleVersionsUiStateFactory
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiAction
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
internal class BibleVersionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val selectedVersion = bibleModel(
        id = SELECTED_ID,
        isSelected = true,
    )
    private val otherVersion = bibleModel(id = OTHER_ID)
    private lateinit var viewModel: BibleVersionViewModel
    private lateinit var downloaderFacade: FakeBibleVersionDownloaderFacade
    private lateinit var bibleRepository: FakeBibleRepository
    private lateinit var permissionRequester: RecordingNotificationPermissionRequester
    private lateinit var bibleVersionDao: InMemoryBibleVersionDao
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var actions: MutableList<BibleVersionUiAction>
    private lateinit var states: MutableList<BibleVersionsUiState>
    private var initializeCalls = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the versions grouped by language`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        val state = states.last()

        // Then
        assertEquals(
            expected = BibleVersionsUiState.Success(
                mapOf(Language.PORTUGUESE_BRAZIL to listOf(selectedVersion, otherVersion)),
            ),
            actual = state,
        )
    }

    @Test
    fun `downloading starts the download tracks it and asks for the notification permission`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(BibleVersionUiEvent.OnDownload(OTHER_ID))

            // Then
            assertEquals(
                expected = listOf("download $OTHER_ID"),
                actual = downloaderFacade.calls,
            )
            assertEquals(
                expected = AnalyticsEventNames.BIBLE_VERSION_DOWNLOAD_STARTED to mapOf<String, Any>(
                    AnalyticsParams.VERSION_ID to OTHER_ID,
                    AnalyticsParams.IS_RESUME to false,
                ),
                actual = trackedEvents.first(),
            )
            assertEquals(
                expected = 1,
                actual = permissionRequester.requestCount,
            )
            assertTrue(actions.isEmpty())
        }

    @Test
    fun `downloading shows the keep the app open tip when the downloader asks for it`() = runTest(testDispatcher) {
        // Given
        prepareScenario(shouldShowDownloadTip = true)

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnDownload(OTHER_ID))

        // Then
        assertEquals(
            expected = listOf<BibleVersionUiAction>(BibleVersionUiAction.ShowDownloadTip),
            actual = actions,
        )
    }

    @Test
    fun `pausing pauses the download`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnPause(OTHER_ID))

        // Then
        assertEquals(
            expected = listOf("pause $OTHER_ID"),
            actual = downloaderFacade.calls,
        )
    }

    @Test
    fun `resuming downloads again as a resume and asks for the notification permission`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnResume(OTHER_ID))

        // Then
        assertEquals(
            expected = listOf("download $OTHER_ID"),
            actual = downloaderFacade.calls,
        )
        assertEquals(
            expected = true,
            actual = trackedEvents.first().second[AnalyticsParams.IS_RESUME],
        )
        assertEquals(
            expected = 1,
            actual = permissionRequester.requestCount,
        )
    }

    @Test
    fun `updating resets the version and downloads it again`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnUpdate(OTHER_ID))

        // Then
        assertEquals(
            expected = DownloadStatus.NOT_STARTED,
            actual = bibleVersionDao.versions[OTHER_ID]?.status,
        )
        assertEquals(
            expected = listOf("download $OTHER_ID"),
            actual = downloaderFacade.calls,
        )
        assertEquals(
            expected = 1,
            actual = permissionRequester.requestCount,
        )
    }

    @Test
    fun `deleting opens the delete version confirmation`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnDelete(OTHER_ID))

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.Navigate(DeleteVersionNavRoute(OTHER_ID))),
            actual = commands,
        )
    }

    @Test
    fun `selecting another version selects it and tracks the selection`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnSelect(OTHER_ID))

        // Then
        assertEquals(
            expected = listOf(OTHER_ID),
            actual = bibleRepository.selectedVersionIds,
        )
        assertEquals(
            expected = listOf(
                AnalyticsEventNames.BIBLE_VERSION_SELECTED to mapOf<String, Any>(
                    AnalyticsParams.VERSION_ID to OTHER_ID,
                ),
            ),
            actual = trackedEvents,
        )
    }

    @Test
    fun `selecting the version already selected does not track it again`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnSelect(SELECTED_ID))

        // Then
        assertEquals(
            expected = listOf(SELECTED_ID),
            actual = bibleRepository.selectedVersionIds,
        )
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `dismissing goes back`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.OnDismiss)

        // Then
        assertEquals(
            expected = listOf<NavigationCommand>(NavigationCommand.NavigateBack),
            actual = commands,
        )
    }

    @Test
    fun `retrying loads the bible versions again`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(BibleVersionUiEvent.TryToDownloadBibleVersionsAgain)

        // Then
        assertEquals(
            expected = 1,
            actual = initializeCalls,
        )
    }

    private fun TestScope.prepareScenario(shouldShowDownloadTip: Boolean = false) {
        downloaderFacade = FakeBibleVersionDownloaderFacade(shouldShowDownloadTip = shouldShowDownloadTip)
        bibleRepository = FakeBibleRepository(listOf(selectedVersion, otherVersion))
        permissionRequester = RecordingNotificationPermissionRequester()
        bibleVersionDao = InMemoryBibleVersionDao(
            listOf(
                BibleVersionEntity(
                    id = OTHER_ID,
                    status = DownloadStatus.DONE,
                ),
            ),
        )
        trackedEvents = mutableListOf()
        commands = mutableListOf()
        actions = mutableListOf()
        states = mutableListOf()
        initializeCalls = 0
        val navigator = Navigator()
        val trackEvent: (String, Map<String, Any>) -> Unit = { name, params -> trackedEvents += name to params }
        viewModel = BibleVersionViewModel(
            setSelectedVersion = SetSelectedVersionUseCase(bibleRepository),
            downloaderFacade = downloaderFacade,
            initializeBibleVersions = { initializeCalls++ },
            updateBibleVersion = UpdateBibleVersionUseCase(
                deleteBibleVersionDownload = DeleteBibleVersionDownloadUseCase(
                    bibleVersionDao = bibleVersionDao,
                    verseDao = NoOpDeleteVerseDao(),
                    notifier = RecordingDownloadNotifier(),
                ),
                downloaderFacade = downloaderFacade,
            ),
            requestDownloadNotificationPermission = RequestDownloadNotificationPermissionUseCase(
                notificationPermissionRequester = permissionRequester,
                trackEvent = { _, _ -> },
                navigator = navigator,
            ),
            navigator = navigator,
            trackEvent = trackEvent,
            uiStateFactory = BibleVersionsUiStateFactory(
                GetBibleVersionsByLanguageUseCase(
                    repository = bibleRepository,
                    getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                ),
            ),
        )
        backgroundScope.launch { navigator.commands.collect { commands += it } }
        backgroundScope.launch { viewModel.uiAction.collect { actions += it } }
        backgroundScope.launch { viewModel.uiState.collect { states += it } }
    }

    private companion object {
        const val SELECTED_ID = "ACF"
        const val OTHER_ID = "NVI"
    }
}
