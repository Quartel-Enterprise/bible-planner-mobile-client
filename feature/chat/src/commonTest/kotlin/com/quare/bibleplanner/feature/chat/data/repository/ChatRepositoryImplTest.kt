package com.quare.bibleplanner.feature.chat.data.repository

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatConversationsRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatDraftLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatMessagesRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatRealtimeDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.FakeChatStreamRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.dto.ChatAcceptedDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDoneDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.data.mapper.ChatContextRequestMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatConversationMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatMessageMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatQuotaMapper
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
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
