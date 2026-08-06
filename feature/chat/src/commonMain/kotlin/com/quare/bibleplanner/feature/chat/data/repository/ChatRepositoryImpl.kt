package com.quare.bibleplanner.feature.chat.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.data.datasource.ChatConversationsRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatMessagesRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatRealtimeDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatStreamRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.dto.AskChatRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatAcceptedDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatRateLimitDto
import com.quare.bibleplanner.feature.chat.data.exception.ChatLimitReachedException
import com.quare.bibleplanner.feature.chat.data.exception.ChatRateLimitedException
import com.quare.bibleplanner.feature.chat.data.mapper.ChatContextRequestMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatConversationMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatMessageMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatQuotaMapper
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Owns the chat session in memory: conversations, their messages and the quota. Messages are keyed
 * by the id the server assigns, which is what makes the two sources of truth converge — the SSE
 * stream of an answer being written here and the Realtime insert of that same row (or of a message
 * from another device) upsert into the same slot instead of producing two bubbles.
 *
 * There is no local database on purpose: the chat needs the network to say anything at all.
 */
internal class ChatRepositoryImpl(
    private val conversationsDataSource: ChatConversationsRemoteDataSource,
    private val messagesDataSource: ChatMessagesRemoteDataSource,
    private val streamDataSource: ChatStreamRemoteDataSource,
    private val realtimeDataSource: ChatRealtimeDataSource,
    private val conversationMapper: ChatConversationMapper,
    private val messageMapper: ChatMessageMapper,
    private val quotaMapper: ChatQuotaMapper,
    private val contextRequestMapper: ChatContextRequestMapper,
    private val languageCodeMapper: LanguageCodeMapper,
    private val bibleRepository: BibleRepository,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val json: Json,
) : ChatRepository {
    private val conversations: MutableStateFlow<Map<String, ChatConversationModel>> = MutableStateFlow(emptyMap())
    private val messages: MutableStateFlow<Map<String, Map<String, ChatMessageModel>>> = MutableStateFlow(emptyMap())
    private val quota: MutableStateFlow<ChatQuotaModel?> = MutableStateFlow(null)
    private var currentUserId: String? = null

    override fun observeConversations(): Flow<List<ChatConversationModel>> = conversations
        .asStateFlow()
        .map { current -> current.values.sortedByDescending(ChatConversationModel::updatedAt) }

    override fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>> = messages
        .asStateFlow()
        .map { current -> current[conversationId].orEmpty().values.sortedWith(messageOrder) }

    override fun observeQuota(): Flow<ChatQuotaModel?> = quota.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun syncRemoteChanges() {
        observeAuthenticatedUserId()
            .onEach(::onUserChanged)
            .filterNotNull()
            .flatMapLatest { userId ->
                merge(
                    realtimeDataSource.observeConversations(userId),
                    realtimeDataSource.observeMessages(userId),
                )
            }.catch { throwable -> Logger.e(throwable) { "Chat realtime sync failed" } }
            .collect(::applyRemoteChange)
    }

    // The session store outlives the screen, so signing out (or into another account) must empty
    // it: nobody should ever see the previous user's conversations.
    private fun onUserChanged(userId: String?) {
        if (userId == currentUserId) return
        currentUserId = userId
        conversations.value = emptyMap()
        messages.value = emptyMap()
        quota.value = null
    }

    private fun applyRemoteChange(change: ChatRemoteChange) {
        when (change) {
            is ChatRemoteChange.ConversationUpserted -> conversationMapper.map(change.conversation).let { model ->
                conversations.update { it + (model.id to model) }
            }

            is ChatRemoteChange.ConversationDeleted -> {
                conversations.update { it - change.conversationId }
                messages.update { it - change.conversationId }
            }

            is ChatRemoteChange.MessageUpserted -> upsertMessage(
                conversationId = change.message.conversationId,
                message = messageMapper.map(change.message),
            )

            is ChatRemoteChange.MessageDeleted -> messages.update { current ->
                current.mapValues { (_, byId) -> byId - change.messageId }
            }
        }
    }

    // A message already in the session keeps its streamed content until the stream finishes, so a
    // Realtime insert arriving mid-answer never truncates the bubble the user is reading.
    private fun upsertMessage(
        conversationId: String,
        message: ChatMessageModel,
    ) {
        messages.update { current ->
            val byId = current[conversationId].orEmpty()
            val existing = byId[message.id]
            val merged = if (existing?.isStreaming == true) existing else message
            current + (conversationId to byId + (message.id to merged))
        }
    }

    override suspend fun refreshConversations() {
        val fetched = conversationsDataSource.fetchAll().map(conversationMapper::map)
        conversations.value = fetched.associateBy(ChatConversationModel::id)
    }

    override suspend fun refreshMessages(conversationId: String) {
        val fetched = messagesDataSource
            .fetchByConversation(conversationId)
            .map(messageMapper::map)
            .associateBy(ChatMessageModel::id)
        messages.update { current ->
            val streaming = current[conversationId].orEmpty().filterValues(ChatMessageModel::isStreaming)
            current + (conversationId to fetched + streaming)
        }
    }

    override suspend fun refreshQuota() {
        streamDataSource.fetchStatus().onSuccess { status -> quota.value = quotaMapper.map(status) }
    }

    override fun sendMessage(request: ChatSendRequestModel): Flow<ChatSendEventModel> {
        var pending: PendingAnswer? = null
        return flow {
            val requestDto = buildRequest(request)
            streamDataSource.streamAnswer(requestDto).collect { event ->
                pending = emitStreamEvent(
                    event = event,
                    request = request,
                    pending = pending,
                )
            }
        }.catch { throwable -> throw mapFailure(throwable, pending) }
    }

    private suspend fun buildRequest(request: ChatSendRequestModel): AskChatRequestDto {
        val languageCode = languageCodeMapper.map(getAppLanguageFlow().first())
        val context = request.context?.takeIf { request.conversationId == null }?.let { model ->
            contextRequestMapper.map(
                passages = model.passages,
                version = bibleRepository.getSelectedVersionIdFlow().first(),
            )
        }
        return AskChatRequestDto(
            conversationId = request.conversationId,
            message = request.message,
            language = languageCode,
            context = context,
        )
    }

    private suspend fun FlowCollector<ChatSendEventModel>.emitStreamEvent(
        event: ChatStreamEvent,
        request: ChatSendRequestModel,
        pending: PendingAnswer?,
    ): PendingAnswer? = when (event) {
        is ChatStreamEvent.Accepted -> onAccepted(
            payload = event.payload,
            question = request.message,
        )

        is ChatStreamEvent.Delta -> pending?.also { answer ->
            updateStreamingMessage(answer) { current -> current + event.text }
        }

        // The model restarted from scratch: what was shown so far is no longer the answer.
        ChatStreamEvent.Restart -> pending?.also { answer -> updateStreamingMessage(answer) { "" } }

        is ChatStreamEvent.Done -> {
            finishStreamingMessage(
                conversationId = event.payload.conversationId,
                messageId = event.payload.assistantMessageId,
                content = event.payload.content,
            )
            quota.value = quotaMapper.map(event.payload)
            emit(ChatSendEventModel.Completed)
            null
        }

        is ChatStreamEvent.Title -> pending.also {
            conversations.update { current ->
                val conversation = current[event.conversationId] ?: return@update current
                current + (event.conversationId to conversation.copy(title = event.title))
            }
        }
    }

    private suspend fun FlowCollector<ChatSendEventModel>.onAccepted(
        payload: ChatAcceptedDto,
        question: String,
    ): PendingAnswer {
        if (payload.isNewConversation) {
            conversationMapper
                .map(
                    conversationId = payload.conversationId,
                    title = payload.title,
                    preview = question,
                    contextLabel = payload.contextLabel,
                ).let { model -> conversations.update { it + (model.id to model) } }
        }
        putMessage(
            conversationId = payload.conversationId,
            message = ChatMessageModel(
                id = payload.userMessageId,
                role = ChatRoleModel.USER,
                content = question,
                isStreaming = false,
                createdAt = Clock.System.now(),
            ),
        )
        putMessage(
            conversationId = payload.conversationId,
            message = ChatMessageModel(
                id = payload.assistantMessageId,
                role = ChatRoleModel.ASSISTANT,
                content = "",
                isStreaming = true,
                createdAt = Clock.System.now(),
            ),
        )
        emit(
            ChatSendEventModel.Accepted(
                conversationId = payload.conversationId,
                isNewConversation = payload.isNewConversation,
            ),
        )
        return PendingAnswer(
            conversationId = payload.conversationId,
            userMessageId = payload.userMessageId,
            assistantMessageId = payload.assistantMessageId,
            isNewConversation = payload.isNewConversation,
        )
    }

    private fun putMessage(
        conversationId: String,
        message: ChatMessageModel,
    ) {
        messages.update { current ->
            current + (conversationId to current[conversationId].orEmpty() + (message.id to message))
        }
    }

    private fun updateStreamingMessage(
        pending: PendingAnswer,
        transform: (String) -> String,
    ) {
        messages.update { current ->
            val byId = current[pending.conversationId].orEmpty()
            val streaming = byId[pending.assistantMessageId] ?: return@update current
            current +
                (
                    pending.conversationId to
                        byId + (streaming.id to streaming.copy(content = transform(streaming.content)))
                )
        }
    }

    private fun finishStreamingMessage(
        conversationId: String,
        messageId: String,
        content: String,
    ) {
        messages.update { current ->
            val byId = current[conversationId].orEmpty()
            val streaming = byId[messageId] ?: return@update current
            val finished = streaming.copy(
                content = content,
                isStreaming = false,
            )
            current + (conversationId to byId + (messageId to finished))
        }
    }

    /**
     * The server drops the question row when a generation fails, so the session drops it too:
     * retrying then re-asks instead of stacking a second copy of the same question.
     */
    private fun discardPendingAnswer(pending: PendingAnswer) {
        messages.update { current ->
            val byId = current[pending.conversationId].orEmpty()
            current + (pending.conversationId to byId - pending.userMessageId - pending.assistantMessageId)
        }
        if (pending.isNewConversation) {
            conversations.update { it - pending.conversationId }
            messages.update { it - pending.conversationId }
        }
    }

    override suspend fun renameConversation(
        conversationId: String,
        title: String,
    ) {
        conversationsDataSource.rename(
            conversationId = conversationId,
            title = title,
        )
        conversations.update { current ->
            val conversation = current[conversationId] ?: return@update current
            current + (conversationId to conversation.copy(title = title))
        }
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversationsDataSource.delete(conversationId)
        conversations.update { it - conversationId }
        messages.update { it - conversationId }
    }

    private suspend fun mapFailure(
        throwable: Throwable,
        pending: PendingAnswer?,
    ): Throwable {
        pending?.let(::discardPendingAnswer)
        throwable.rateLimitRetrySeconds()?.let { seconds -> return ChatRateLimitedException(seconds) }
        if (throwable.isLimitReached()) return ChatLimitReachedException()
        Logger.e(throwable) { "Failed to send the chat message" }
        return throwable
    }

    private fun Throwable.statusCode(): Int? = when (this) {
        is RestException -> statusCode
        is SSEClientException -> response?.status?.value
        else -> null
    }

    private fun Throwable.isLimitReached(): Boolean = statusCode() == LIMIT_EXCEEDED_STATUS

    private suspend fun Throwable.rateLimitRetrySeconds(): Int? {
        if (statusCode() != RATE_LIMITED_STATUS) return null
        val body = suspendRunCatching { (this as? SSEClientException)?.response?.bodyAsText() }.getOrNull()
            ?: return DEFAULT_RETRY_SECONDS
        return suspendRunCatching { json.decodeFromString(ChatRateLimitDto.serializer(), body).retryAfterSeconds }
            .getOrDefault(DEFAULT_RETRY_SECONDS)
    }

    private data class PendingAnswer(
        val conversationId: String,
        val userMessageId: String,
        val assistantMessageId: String,
        val isNewConversation: Boolean,
    )

    private companion object {
        const val LIMIT_EXCEEDED_STATUS = 402
        const val RATE_LIMITED_STATUS = 429
        const val DEFAULT_RETRY_SECONDS = 30

        val messageOrder: Comparator<ChatMessageModel> = compareBy(
            { it.createdAt },
            { it.id },
        )
    }
}
