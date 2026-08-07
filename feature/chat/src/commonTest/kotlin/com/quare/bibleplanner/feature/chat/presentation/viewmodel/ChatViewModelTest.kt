package com.quare.bibleplanner.feature.chat.presentation.viewmodel

import com.quare.bibleplanner.core.model.loginwarning.LoginWarningReason
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.feature.chat.domain.coordinator.FakeChatStreamCoordinator
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendFailureModel
import com.quare.bibleplanner.feature.chat.domain.repository.FakeChatRepository
import com.quare.bibleplanner.feature.chat.domain.usecase.ChatUseCases
import com.quare.bibleplanner.feature.chat.domain.usecase.DeleteChatConversationUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.LoadChatMessagesUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.ObserveChatConversationsUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.ObserveChatMessagesUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.ObserveChatQuotaUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.RefreshChatConversationsUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.RefreshChatQuotaUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.RenameChatConversationUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.SyncChatRemoteChangesUseCase
import com.quare.bibleplanner.feature.chat.presentation.mapper.ChatConversationGroupMapper
import com.quare.bibleplanner.feature.chat.presentation.mapper.ChatMessageUiMapper
import com.quare.bibleplanner.feature.chat.presentation.model.ChatInputMode
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiAction
import com.quare.bibleplanner.feature.chat.presentation.model.ChatUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

private const val SUGGESTION = "Por que Caim matou Abel?"
private const val STARTER = "Resuma esta leitura"

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = FakeChatRepository()
    private val coordinator = FakeChatStreamCoordinator()
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
    private val authenticatedUserId = MutableStateFlow<String?>("user-1")
    private var chatContext: ChatContextModel? = null
    private var chatSuggestions: List<String> = emptyList()
    private var defaultSuggestions: List<String> = emptyList()
    private var entrySource: ChatEntrySource = ChatEntrySource.DAY_STUDY_QUESTIONS

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN typed text WHEN sending THEN the request reaches the coordinator and the input clears`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onEvent(ChatUiEvent.OnInputChanged("Por que Caim matou Abel?"))
            viewModel.onEvent(ChatUiEvent.OnSendClick)

            assertEquals("Por que Caim matou Abel?", coordinator.startedRequests.single().message)
            assertNull(coordinator.startedRequests.single().conversationId)
            assertEquals("", viewModel.uiState.value.input)
            assertTrue(trackedEvents.any { it.first == AnalyticsEventNames.AI_CHAT_MESSAGE_SENT })
        }

    @Test
    fun `GIVEN a signed-out reader WHEN sending THEN login is asked`() = runTest(testDispatcher) {
        authenticatedUserId.value = null
        val viewModel = createViewModel()
        val actions = mutableListOf<ChatUiAction>()
        val collection = launch { viewModel.uiAction.collect(actions::add) }

        viewModel.onEvent(ChatUiEvent.OnInputChanged("Por que Caim matou Abel?"))
        viewModel.onEvent(ChatUiEvent.OnSendClick)

        assertTrue(coordinator.startedRequests.isEmpty())
        assertEquals(
            listOf(LoginWarningNavRoute(LoginWarningReason.AiChat.key)),
            actions.filterIsInstance<ChatUiAction.NavigateToRoute>().map { it.route },
        )
        assertTrue(trackedEvents.none { it.first == AnalyticsEventNames.AI_CHAT_MESSAGE_SENT })
        collection.cancel()
    }

    @Test
    fun `GIVEN a signed-out reader WHEN they sign in THEN the history and quota load`() = runTest(testDispatcher) {
        authenticatedUserId.value = null
        createViewModel()
        assertTrue(repository.refreshedConversations == 0)

        authenticatedUserId.value = "user-1"

        assertTrue(repository.refreshedConversations > 0)
        assertTrue(repository.refreshedQuota > 0)
    }

    @Test
    fun `GIVEN the study has no questions THEN the starters stand in`() = runTest(testDispatcher) {
        entrySource = ChatEntrySource.DAY_STUDY_QUESTIONS
        chatContext = readingContext()
        chatSuggestions = emptyList()
        defaultSuggestions = listOf(STARTER)

        val viewModel = createViewModel()

        assertEquals(listOf(STARTER), viewModel.uiState.value.suggestions)
    }

    @Test
    fun `GIVEN the chat opened from the study THEN its questions are offered`() = runTest(testDispatcher) {
        entrySource = ChatEntrySource.DAY_STUDY_QUESTIONS
        chatContext = readingContext()
        chatSuggestions = listOf(SUGGESTION)
        defaultSuggestions = listOf(STARTER)

        val viewModel = createViewModel()

        assertEquals(listOf(SUGGESTION), viewModel.uiState.value.suggestions)
    }

    @Test
    fun `GIVEN the chat opened from the day THEN the starters are offered`() = runTest(testDispatcher) {
        entrySource = ChatEntrySource.DAY_FAB
        chatContext = readingContext()
        chatSuggestions = listOf(SUGGESTION)
        defaultSuggestions = listOf(STARTER)

        val viewModel = createViewModel()

        assertEquals(listOf(STARTER), viewModel.uiState.value.suggestions)
    }

    @Test
    fun `GIVEN a signed-out reader WHEN tapping a suggestion THEN the chip is kept`() = runTest(testDispatcher) {
        authenticatedUserId.value = null
        val viewModel = createViewModelWithSuggestion()

        viewModel.onEvent(ChatUiEvent.OnSuggestionClick(SUGGESTION))

        assertTrue(coordinator.startedRequests.isEmpty())
        assertEquals(listOf(SUGGESTION), viewModel.uiState.value.suggestions)
    }

    @Test
    fun `GIVEN a signed-in reader WHEN tapping a suggestion THEN it is sent and spent`() = runTest(testDispatcher) {
        val viewModel = createViewModelWithSuggestion()

        viewModel.onEvent(ChatUiEvent.OnSuggestionClick(SUGGESTION))

        assertEquals(SUGGESTION, coordinator.startedRequests.single().message)
        assertTrue(
            viewModel.uiState.value.suggestions
                .isEmpty(),
        )
    }

    @Test
    fun `GIVEN blank text WHEN sending THEN nothing is sent`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEvent(ChatUiEvent.OnInputChanged("   "))
        viewModel.onEvent(ChatUiEvent.OnSendClick)

        assertTrue(coordinator.startedRequests.isEmpty())
    }

    @Test
    fun `GIVEN a conversation accepted by the server WHEN it streams THEN its messages are shown`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            repository.messages.value = mapOf(
                "conversation-1" to listOf(
                    ChatMessageModel(
                        id = "message-1",
                        role = ChatRoleModel.ASSISTANT,
                        content = "Caim matou Abel por inveja.",
                        isStreaming = false,
                        createdAt = Instant.parse("2026-08-06T15:00:00Z"),
                    ),
                ),
            )

            viewModel.onEvent(ChatUiEvent.OnInputChanged("Por que?"))
            viewModel.onEvent(ChatUiEvent.OnSendClick)
            coordinator.accept("conversation-1")

            assertEquals(
                listOf("message-1"),
                viewModel.uiState.value.messages
                    .map { it.id },
            )
        }

    @Test
    fun `GIVEN an exhausted quota WHEN it arrives THEN the input locks and sending is refused`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            repository.quota.value = ChatQuotaModel(
                usedCount = 10,
                freeLimit = 10,
                isPro = false,
            )
            viewModel.onEvent(ChatUiEvent.OnInputChanged("Mais uma pergunta"))
            viewModel.onEvent(ChatUiEvent.OnSendClick)

            assertEquals(ChatInputMode.LOCKED, viewModel.uiState.value.inputMode)
            assertTrue(coordinator.startedRequests.isEmpty())
        }

    @Test
    fun `GIVEN a free user with questions left WHEN quota arrives THEN the remaining count is shown`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            repository.quota.value = ChatQuotaModel(
                usedCount = 7,
                freeLimit = 10,
                isPro = false,
            )

            assertEquals(
                3,
                viewModel.uiState.value.quota
                    ?.remainingFree,
            )
            assertEquals(ChatInputMode.ENABLED, viewModel.uiState.value.inputMode)
        }

    @Test
    fun `GIVEN a pro user WHEN quota arrives THEN no quota footer is shown`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        repository.quota.value = ChatQuotaModel(
            usedCount = 42,
            freeLimit = 10,
            isPro = true,
        )

        assertNull(viewModel.uiState.value.quota)
        assertEquals(ChatInputMode.ENABLED, viewModel.uiState.value.inputMode)
    }

    @Test
    fun `GIVEN an answer streaming WHEN sending again THEN the text is kept`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEvent(ChatUiEvent.OnInputChanged("Primeira"))
        viewModel.onEvent(ChatUiEvent.OnSendClick)
        viewModel.onEvent(ChatUiEvent.OnInputChanged("Segunda"))
        viewModel.onEvent(ChatUiEvent.OnSendClick)

        assertEquals(1, coordinator.startedRequests.size)
        assertEquals("Segunda", viewModel.uiState.value.input)
    }

    @Test
    fun `GIVEN a question just sent WHEN not echoed back yet THEN it shows locally`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEvent(ChatUiEvent.OnInputChanged("Por que Caim matou Abel?"))
        viewModel.onEvent(ChatUiEvent.OnSendClick)

        assertEquals("Por que Caim matou Abel?", viewModel.uiState.value.visiblePendingQuestion)
        assertTrue(viewModel.uiState.value.isThinking)
    }

    @Test
    fun `GIVEN a rate-limited answer WHEN it fails THEN the input enters the cooldown state`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onEvent(ChatUiEvent.OnInputChanged("Pergunta"))
            viewModel.onEvent(ChatUiEvent.OnSendClick)
            coordinator.fail(ChatSendFailureModel.RateLimited(retryAfterSeconds = 12))

            assertEquals(ChatInputMode.COOLDOWN, viewModel.uiState.value.inputMode)
            assertEquals(12, viewModel.uiState.value.cooldownSeconds)
        }

    @Test
    fun `GIVEN the subscribe action WHEN clicked THEN the paywall click is tracked`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEvent(ChatUiEvent.OnSubscribeClick)

        assertTrue(trackedEvents.any { it.first == AnalyticsEventNames.AI_CHAT_SUBSCRIBE_CLICKED })
    }

    @Test
    fun `GIVEN a conversation being renamed WHEN confirmed THEN the new title is persisted`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onEvent(ChatUiEvent.OnRenameConversationClick("conversation-1"))
            viewModel.onEvent(ChatUiEvent.OnRenameDraftChanged("  Caim e Abel  "))
            viewModel.onEvent(ChatUiEvent.OnRenameConfirm)

            assertEquals(listOf("conversation-1" to "Caim e Abel"), repository.renamed)
            assertNull(viewModel.uiState.value.history.renamingId)
        }

    @Test
    fun `GIVEN a blank rename WHEN confirmed THEN the old title is kept`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEvent(ChatUiEvent.OnRenameConversationClick("conversation-1"))
        viewModel.onEvent(ChatUiEvent.OnRenameDraftChanged("   "))
        viewModel.onEvent(ChatUiEvent.OnRenameConfirm)

        assertTrue(repository.renamed.isEmpty())
    }

    @Test
    fun `GIVEN a conversation marked for deletion WHEN confirmed THEN it is deleted`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEvent(ChatUiEvent.OnDeleteConversationClick("conversation-1"))
        viewModel.onEvent(ChatUiEvent.OnDeleteConfirm)

        assertEquals(listOf("conversation-1"), repository.deleted)
        assertNull(viewModel.uiState.value.history.deletingId)
    }

    @Test
    fun `GIVEN a conversation picked from the history WHEN clicked THEN its messages load and the drawer closes`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onEvent(ChatUiEvent.OnHistoryClick)
            viewModel.onEvent(ChatUiEvent.OnConversationClick("conversation-1"))

            assertEquals(listOf("conversation-1"), repository.loadedConversationIds)
            assertEquals(false, viewModel.uiState.value.history.isOpen)
        }

    private fun createViewModelWithSuggestion(): ChatViewModel {
        entrySource = ChatEntrySource.DAY_STUDY_QUESTIONS
        chatContext = readingContext()
        chatSuggestions = listOf(SUGGESTION)
        return createViewModel()
    }

    private fun readingContext(): ChatContextModel = ChatContextModel(
        label = "Gênesis 4-7",
        passages = emptyList(),
    )

    private fun createViewModel(): ChatViewModel = ChatViewModel(
        route = ChatNavRoute(
            source = entrySource,
            dayNumber = null,
            weekNumber = null,
            readingPlanType = null,
        ),
        observeAuthenticatedUserId = { authenticatedUserId },
        getDefaultSuggestions = { defaultSuggestions },
        useCases = ChatUseCases(
            observeConversations = ObserveChatConversationsUseCase(repository),
            observeMessages = ObserveChatMessagesUseCase(repository),
            observeQuota = ObserveChatQuotaUseCase(repository),
            syncRemoteChanges = SyncChatRemoteChangesUseCase(repository),
            refreshConversations = RefreshChatConversationsUseCase(repository),
            loadMessages = LoadChatMessagesUseCase(repository),
            refreshQuota = RefreshChatQuotaUseCase(repository),
            renameConversation = RenameChatConversationUseCase(repository),
            deleteConversation = DeleteChatConversationUseCase(repository),
            getSuggestions = { chatSuggestions },
            getContext = { chatContext },
        ),
        coordinator = coordinator,
        messageUiMapper = ChatMessageUiMapper(),
        conversationGroupMapper = ChatConversationGroupMapper(),
        trackEvent = { name, params -> trackedEvents += name to params },
    )
}
