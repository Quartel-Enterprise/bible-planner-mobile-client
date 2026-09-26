package com.quare.bibleplanner.feature.chat.data.repository

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatConversationsRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatDraftLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatMessagesRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatRealtimeDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatStreamRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.dto.ChatAcceptedDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDoneDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatStatusDto
import com.quare.bibleplanner.feature.chat.data.mapper.ChatContextRequestMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatConversationMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatMessageMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatQuotaMapper
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatRepositoryImplTest {
    private val localDataSource = FakeChatLocalDataSource()
    private val draftDataSource = FakeChatDraftLocalDataSource()
    private val conversationsDataSource = FakeChatConversationsRemoteDataSource()
    private val messagesDataSource = FakeChatMessagesRemoteDataSource()
    private val streamDataSource = FakeChatStreamRemoteDataSource()
    private val realtimeDataSource = FakeChatRealtimeDataSource()
    private val authenticatedUserId = MutableStateFlow<String?>("user-1")

    @Test
    fun `GIVEN an accepted question WHEN observing THEN the streamed answer grows over the cache`() = runTest {
        val repository = createRepository()
        val send = launch(UnconfinedTestDispatcher(testScheduler)) { repository.sendMessage(request()).first { false } }

        streamDataSource.emit(accepted())
        streamDataSource.emit(ChatStreamEvent.Delta("Caim "))
        streamDataSource.emit(ChatStreamEvent.Delta("matou"))

        val thread = repository.observeMessages("conversation-1").first()
        assertEquals(listOf("question-1", "answer-1"), thread.map { it.id })
        assertEquals("Caim matou", thread.last().content)
        assertTrue(thread.last().isStreaming)
        send.cancel()
    }

    @Test
    fun `GIVEN a finished answer WHEN observing THEN it is cached after its question`() = runTest {
        val repository = createRepository()
        val send = launch(UnconfinedTestDispatcher(testScheduler)) { repository.sendMessage(request()).collect {} }

        streamDataSource.emit(accepted())
        streamDataSource.emit(ChatStreamEvent.Delta("Caim matou"))
        streamDataSource.emit(done())

        val thread = repository.observeMessages("conversation-1").first()
        assertEquals(listOf("question-1", "answer-1"), thread.map { it.id })
        assertEquals("Caim matou Abel por inveja.", thread.last().content)
        assertTrue(!thread.last().isStreaming)
        assertTrue(thread.last().createdAt > thread.first().createdAt)
        assertEquals(1, repository.observeQuota().first()?.usedCount)
        send.cancel()
    }

    @Test
    fun `GIVEN a finishing answer WHEN observing THEN it never leaves the thread`() = runTest {
        val repository = createRepository()
        val send = launch(UnconfinedTestDispatcher(testScheduler)) { repository.sendMessage(request()).collect {} }
        streamDataSource.emit(accepted())
        streamDataSource.emit(ChatStreamEvent.Delta("Caim matou"))
        val threads = mutableListOf<List<String>>()
        val observation = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeMessages("conversation-1").collect { thread -> threads += thread.map { it.id } }
        }

        streamDataSource.emit(done())

        assertTrue(threads.all { thread -> thread.lastOrNull() == "answer-1" })
        observation.cancel()
        send.cancel()
    }

    @Test
    fun `GIVEN a failed generation WHEN it dies THEN the thread is re-read from the server`() = runTest {
        val repository = createRepository()
        messagesDataSource.remoteMessages = mapOf("conversation-1" to listOf(questionDto()))
        var failure: Throwable? = null
        val send = launch(UnconfinedTestDispatcher(testScheduler)) {
            runCatching { repository.sendMessage(request()).collect {} }
                .onFailure { error -> failure = error }
        }

        streamDataSource.emit(accepted())
        streamDataSource.fail(RuntimeException("stream died"))

        val thread = repository.observeMessages("conversation-1").first()
        assertEquals(listOf("question-1"), thread.map { it.id })
        assertTrue(thread.none { it.isStreaming })
        assertTrue(failure is RuntimeException)
        send.cancel()
    }

    @Test
    fun `GIVEN a realtime deletion WHEN applied THEN the conversation leaves the mirror`() = runTest {
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }

        realtimeDataSource.conversationChanges.emit(ChatRemoteChange.ConversationDeleted("conversation-1"))

        assertTrue(localDataSource.conversations.value.isEmpty())
        sync.cancel()
    }

    @Test
    fun `GIVEN a message of an unknown conversation WHEN it arrives THEN the list is pulled first`() = runTest {
        val repository = createRepository()
        conversationsDataSource.remoteConversations = emptyList()
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }

        realtimeDataSource.messageChanges.emit(ChatRemoteChange.MessageUpserted(questionDto()))

        assertEquals(1, conversationsDataSource.fetchCount)
        assertEquals(
            listOf("question-1"),
            localDataSource.messages.value["conversation-1"]
                .orEmpty()
                .map { it.id },
        )
        sync.cancel()
    }

    @Test
    fun `GIVEN another account signs in WHEN syncing THEN the previous mirror is wiped`() = runTest {
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }

        authenticatedUserId.value = "user-2"

        assertTrue(localDataSource.conversations.value.isEmpty())
        sync.cancel()
    }

    @Test
    fun `GIVEN the same account WHEN it re-emits THEN the mirror survives`() = runTest {
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }

        authenticatedUserId.value = null
        authenticatedUserId.value = "user-1"

        assertEquals(1, localDataSource.conversations.value.size)
        sync.cancel()
    }

    @Test
    fun `GIVEN a deleted conversation THEN its draft goes with it`() = runTest {
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))
        draftDataSource.drafts.value = mapOf("conversation-1" to "Por que")

        repository.deleteConversation("conversation-1")

        assertEquals(listOf("conversation-1"), conversationsDataSource.deleted)
        assertTrue(localDataSource.conversations.value.isEmpty())
        assertTrue(draftDataSource.drafts.value.isEmpty())
    }

    @Test
    fun `GIVEN cached conversations WHEN observing them THEN reads the cache`() = runTest {
        // Given
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))

        // When
        val conversations = repository.observeConversations().first()

        // Then
        assertEquals(
            expected = listOf("conversation-1"),
            actual = conversations.map { it.id },
        )
    }

    @Test
    fun `GIVEN a running sync WHEN realtime connects THEN the conversation list is pulled`() = runTest {
        // Given
        val repository = createRepository()
        conversationsDataSource.remoteConversations = listOf(conversationDto())
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }

        // When
        realtimeDataSource.connections.emit(Unit)

        // Then
        assertEquals(
            expected = listOf("conversation-1"),
            actual = localDataSource.conversations.value.map { it.id },
        )
        sync.cancel()
    }

    @Test
    fun `GIVEN a failing pull WHEN realtime reconnects THEN the sync keeps pulling`() = runTest {
        // Given
        val repository = createRepository()
        conversationsDataSource.fetchFailure = RuntimeException("offline")
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }
        realtimeDataSource.connections.emit(Unit)

        // When
        realtimeDataSource.connections.emit(Unit)

        // Then
        assertEquals(
            expected = 2,
            actual = conversationsDataSource.fetchCount,
        )
        sync.cancel()
    }

    @Test
    fun `GIVEN a running sync WHEN a conversation is upserted remotely THEN it is cached`() = runTest {
        // Given
        val repository = createRepository()
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }

        // When
        realtimeDataSource.conversationChanges.emit(ChatRemoteChange.ConversationUpserted(conversationDto()))

        // Then
        assertEquals(
            expected = listOf("Caim e Abel"),
            actual = localDataSource.conversations.value.map { it.title },
        )
        sync.cancel()
    }

    @Test
    fun `GIVEN a cached message WHEN it is deleted remotely THEN it leaves the thread`() = runTest {
        // Given
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))
        val sync = launch(UnconfinedTestDispatcher(testScheduler)) { repository.syncRemoteChanges() }
        realtimeDataSource.messageChanges.emit(ChatRemoteChange.MessageUpserted(questionDto()))

        // When
        realtimeDataSource.messageChanges.emit(ChatRemoteChange.MessageDeleted("question-1"))

        // Then
        assertTrue(
            localDataSource.messages.value["conversation-1"]
                .orEmpty()
                .isEmpty(),
        )
        sync.cancel()
    }

    @Test
    fun `GIVEN the server status WHEN refreshing the quota THEN it is exposed`() = runTest {
        // Given
        val repository = createRepository()
        streamDataSource.status = ChatStatusDto(
            usedCount = 3,
            freeLimit = 10,
            isPro = false,
        )

        // When
        repository.refreshQuota()

        // Then
        assertEquals(
            expected = ChatQuotaModel(
                usedCount = 3,
                freeLimit = 10,
                isPro = false,
            ),
            actual = repository.observeQuota().first(),
        )
    }

    @Test
    fun `GIVEN a question about a reading WHEN it opens a conversation THEN the reading goes along`() = runTest {
        // Given
        val repository = createRepository()

        // When
        val send = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.sendMessage(request().copy(context = readingContext())).collect {}
        }

        // Then
        val context = streamDataSource.requests.single().context
        assertEquals(
            expected = "ACF",
            actual = context?.version,
        )
        assertEquals(
            expected = listOf("GENESIS"),
            actual = context?.passages?.map { it.book },
        )
        assertEquals(
            expected = 4,
            actual = context?.dayNumber,
        )
        assertEquals(
            expected = "pt-BR",
            actual = streamDataSource.requests.single().language,
        )
        send.cancel()
    }

    @Test
    fun `GIVEN a follow-up question WHEN sending it THEN the reading is not sent again`() = runTest {
        // Given
        val repository = createRepository()

        // When
        val send = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository
                .sendMessage(
                    request().copy(
                        conversationId = "conversation-1",
                        context = readingContext(),
                    ),
                ).collect {}
        }

        // Then
        assertEquals(
            expected = null,
            actual = streamDataSource.requests.single().context,
        )
        send.cancel()
    }

    @Test
    fun `GIVEN a streaming answer WHEN the server restarts it THEN the partial text is dropped`() = runTest {
        // Given
        val repository = createRepository()
        val send = launch(UnconfinedTestDispatcher(testScheduler)) { repository.sendMessage(request()).collect {} }
        streamDataSource.emit(accepted())
        streamDataSource.emit(ChatStreamEvent.Delta("Caim"))

        // When
        streamDataSource.emit(ChatStreamEvent.Restart)

        // Then
        assertEquals(
            expected = "",
            actual = repository
                .observeMessages("conversation-1")
                .first()
                .last()
                .content,
        )
        send.cancel()
    }

    @Test
    fun `GIVEN a new conversation WHEN its title is generated THEN the cached conversation is renamed`() = runTest {
        // Given
        val repository = createRepository()
        val send = launch(UnconfinedTestDispatcher(testScheduler)) { repository.sendMessage(request()).collect {} }
        streamDataSource.emit(accepted())

        // When
        streamDataSource.emit(
            ChatStreamEvent.Title(
                conversationId = "conversation-1",
                title = "A inveja de Caim",
            ),
        )

        // Then
        assertEquals(
            expected = listOf("A inveja de Caim"),
            actual = localDataSource.conversations.value.map { it.title },
        )
        send.cancel()
    }

    @Test
    fun `GIVEN a cached conversation WHEN renaming it THEN the server and the cache get the new title`() = runTest {
        // Given
        val repository = createRepository()
        localDataSource.conversations.value = listOf(conversation("conversation-1"))

        // When
        repository.renameConversation(
            conversationId = "conversation-1",
            title = "A inveja de Caim",
        )

        // Then
        assertEquals(
            expected = listOf("conversation-1" to "A inveja de Caim"),
            actual = conversationsDataSource.renamed,
        )
        assertEquals(
            expected = listOf("A inveja de Caim"),
            actual = localDataSource.conversations.value.map { it.title },
        )
    }

    @Test
    fun `GIVEN an uncached conversation WHEN renaming it THEN only the server is updated`() = runTest {
        // Given
        val repository = createRepository()

        // When
        repository.renameConversation(
            conversationId = "conversation-1",
            title = "A inveja de Caim",
        )

        // Then
        assertEquals(
            expected = listOf("conversation-1" to "A inveja de Caim"),
            actual = conversationsDataSource.renamed,
        )
        assertTrue(localDataSource.conversations.value.isEmpty())
    }

    @Test
    fun `GIVEN typed text WHEN saving the draft THEN it is read back for its thread`() = runTest {
        // Given
        val repository = createRepository()

        // When
        repository.saveDraft(
            PendingDraftModel(
                threadKey = "conversation-1",
                content = "Por que",
            ),
        )

        // Then
        assertEquals(
            expected = "Por que",
            actual = repository.observeDraft("conversation-1").first(),
        )
    }

    private fun createRepository(): ChatRepositoryImpl = ChatRepositoryImpl(
        localDataSource = localDataSource,
        draftDataSource = draftDataSource,
        conversationsDataSource = conversationsDataSource,
        messagesDataSource = messagesDataSource,
        streamDataSource = streamDataSource,
        realtimeDataSource = realtimeDataSource,
        conversationMapper = ChatConversationMapper(),
        messageMapper = ChatMessageMapper(),
        quotaMapper = ChatQuotaMapper(),
        contextRequestMapper = ChatContextRequestMapper(BookIdWireNameMapper()),
        languageCodeMapper = LanguageCodeMapper(),
        bibleRepository = FakeBibleRepository(),
        getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
        observeAuthenticatedUserId = { authenticatedUserId },
        json = Json { ignoreUnknownKeys = true },
    )

    private fun request(): ChatSendRequestModel = ChatSendRequestModel(
        conversationId = null,
        message = "Por que Caim matou Abel?",
        context = null,
    )

    private fun accepted(): ChatStreamEvent.Accepted = ChatStreamEvent.Accepted(
        ChatAcceptedDto(
            conversationId = "conversation-1",
            userMessageId = "question-1",
            assistantMessageId = "answer-1",
            isNewConversation = true,
            contextLabel = null,
            title = "Caim e Abel",
        ),
    )

    private fun done(): ChatStreamEvent.Done = ChatStreamEvent.Done(
        ChatDoneDto(
            conversationId = "conversation-1",
            assistantMessageId = "answer-1",
            content = "Caim matou Abel por inveja.",
            usedCount = 1,
            freeLimit = 10,
            isPro = false,
        ),
    )

    private fun questionDto(): ChatMessageDto = ChatMessageDto(
        id = "question-1",
        conversationId = "conversation-1",
        role = "user",
        content = "Por que Caim matou Abel?",
        createdAt = "2026-08-14T12:00:00Z",
        status = "complete",
    )

    private fun conversationDto(): ChatConversationDto = ChatConversationDto(
        id = "conversation-1",
        title = "Caim e Abel",
        preview = "Por que Caim matou Abel?",
        contextType = null,
        context = null,
        updatedAt = "2026-08-14T12:00:00Z",
    )

    private fun readingContext(): ChatContextModel = ChatContextModel(
        label = "Gênesis 4",
        passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(
                    ChapterModel(
                        number = 4,
                        startVerse = null,
                        endVerse = null,
                        bookId = BookId.GEN,
                    ),
                ),
                isRead = false,
                chapterRanges = "4",
            ),
        ),
        planDay = ChatPlanDayModel(
            dayNumber = 4,
            weekNumber = 1,
            readingPlanType = "CHRONOLOGICAL",
        ),
    )

    private fun conversation(id: String): ChatConversationModel = ChatConversationModel(
        id = id,
        title = "Caim e Abel",
        preview = "Por que Caim matou Abel?",
        contextLabel = null,
        planDay = null,
        updatedAt = Instant.parse("2026-08-14T12:00:00Z"),
    )
}

private class FakeBibleRepository : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(emptyList())

    override fun getSelectedVersionIdFlow(): Flow<String> = flowOf("ACF")

    override suspend fun setSelectedVersionId(id: String) = Unit
}
