package com.quare.bibleplanner.feature.chat.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.data.datasource.ChatConversationsRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatDraftLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatMessagesRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatRealtimeDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatStreamRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.dto.AskChatRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatAcceptedDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatRateLimitDto
import com.quare.bibleplanner.feature.chat.data.exception.ChatConversationGoneException
import com.quare.bibleplanner.feature.chat.data.exception.ChatLimitReachedException
import com.quare.bibleplanner.feature.chat.data.exception.ChatRateLimitedException
import com.quare.bibleplanner.feature.chat.data.mapper.ChatContextRequestMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatConversationMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatMessageMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatQuotaMapper
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import com.quare.bibleplanner.feature.chat.data.model.StreamingAnswer
import com.quare.bibleplanner.feature.chat.data.model.withStreamingAnswer
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class ChatRepositoryImpl(
    private val localDataSource: ChatLocalDataSource,
    private val draftDataSource: ChatDraftLocalDataSource,
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
    private val streamingAnswer: MutableStateFlow<StreamingAnswer?> = MutableStateFlow(null)
    private val quota: MutableStateFlow<ChatQuotaModel?> = MutableStateFlow(null)
    private var currentUserId: String? = null

    override fun observeConversations(): Flow<List<ChatConversationModel>> = localDataSource.observeConversations()

    override fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>> = combine(
        localDataSource.observeMessages(conversationId),
        streamingAnswer.asStateFlow(),
    ) { cached, streaming ->
        cached.withStreamingAnswer(
            streaming = streaming,
            conversationId = conversationId,
        )
    }

    override fun observeQuota(): Flow<ChatQuotaModel?> = quota.asStateFlow()

    override suspend fun syncRemoteChanges() {
        observeAuthenticatedUserId()
            .onEach(::onUserChanged)
            .collectLatest { userId ->
                if (userId == null) return@collectLatest
                coroutineScope {
                    launch { observeRemoteChanges(userId) }
                    launch { pullOnConnected() }
                }
            }
    }

    private suspend fun observeRemoteChanges(userId: String) {
        merge(
            realtimeDataSource.observeConversations(userId),
            realtimeDataSource.observeMessages(userId),
        ).catch { throwable -> Logger.e(throwable) { "Chat realtime sync failed" } }
            .collect(::applyRemoteChange)
    }

    private suspend fun pullOnConnected() {
        realtimeDataSource.observeConnected().collect {
            suspendRunCatching { refreshConversations() }
                .onFailure { error -> Logger.e(error) { "Failed to pull the chat snapshot" } }
        }
    }

    private suspend fun onUserChanged(userId: String?) {
        val previousUserId = currentUserId
        currentUserId = userId
        if (previousUserId == null || userId == null || previousUserId == userId) return
        quota.value = null
        localDataSource.deleteAll()
    }

    private suspend fun applyRemoteChange(change: ChatRemoteChange) {
        suspendRunCatching { applyChange(change) }
            .onFailure { error -> Logger.e(error) { "Failed to apply a remote chat change" } }
    }

    private suspend fun applyChange(change: ChatRemoteChange) {
        when (change) {
            is ChatRemoteChange.ConversationUpserted ->
                localDataSource.saveConversation(conversationMapper.map(change.conversation))

            is ChatRemoteChange.ConversationDeleted -> localDataSource.deleteConversation(change.conversationId)

            is ChatRemoteChange.MessageUpserted -> onRemoteMessage(change)

            is ChatRemoteChange.MessageDeleted -> localDataSource.deleteMessage(change.messageId)
        }
    }

    private suspend fun onRemoteMessage(change: ChatRemoteChange.MessageUpserted) {
        val conversationId = change.message.conversationId
        if (findConversation(conversationId) == null) refreshConversations()
        localDataSource.saveMessage(
            conversationId = conversationId,
            message = messageMapper.map(change.message),
        )
    }

    override suspend fun refreshConversations() {
        localDataSource.replaceConversations(conversationsDataSource.fetchAll().map(conversationMapper::map))
    }

    override suspend fun refreshMessages(conversationId: String) {
        localDataSource.replaceMessages(
            conversationId = conversationId,
            messages = messagesDataSource.fetchByConversation(conversationId).map(messageMapper::map),
        )
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
        }.catch { throwable ->
            throw mapFailure(
                throwable = throwable,
                pending = pending,
                conversationId = request.conversationId,
            )
        }
    }

    private suspend fun buildRequest(request: ChatSendRequestModel): AskChatRequestDto {
        val languageCode = languageCodeMapper.map(getAppLanguageFlow().first())
        val context = request.context?.takeIf { request.conversationId == null }?.let { model ->
            contextRequestMapper.map(
                passages = model.passages,
                version = bibleRepository.getSelectedVersionIdFlow().first(),
                planDay = model.planDay,
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
            planDay = request.context?.planDay,
        )

        is ChatStreamEvent.Delta -> pending?.also {
            streamingAnswer.update { current -> current?.copy(content = current.content + event.text) }
        }

        ChatStreamEvent.Restart -> pending?.also {
            streamingAnswer.update { current -> current?.copy(content = "") }
        }

        is ChatStreamEvent.Done -> {
            onDone(
                conversationId = event.payload.conversationId,
                messageId = event.payload.assistantMessageId,
                content = event.payload.content,
            )
            quota.value = quotaMapper.map(event.payload)
            emit(ChatSendEventModel.Completed)
            null
        }

        is ChatStreamEvent.Title -> pending.also { onTitleGenerated(event.conversationId, event.title) }
    }

    private suspend fun FlowCollector<ChatSendEventModel>.onAccepted(
        payload: ChatAcceptedDto,
        question: String,
        planDay: ChatPlanDayModel?,
    ): PendingAnswer {
        val now = Clock.System.now()
        if (payload.isNewConversation) {
            localDataSource.saveConversation(
                conversationMapper.map(
                    conversationId = payload.conversationId,
                    title = payload.title,
                    preview = question,
                    contextLabel = payload.contextLabel,
                    planDay = planDay,
                ),
            )
        }
        localDataSource.saveMessage(
            conversationId = payload.conversationId,
            message = ChatMessageModel(
                id = payload.userMessageId,
                role = ChatRoleModel.USER,
                content = question,
                isStreaming = false,
                isFailed = false,
                createdAt = now,
            ),
        )
        streamingAnswer.value = StreamingAnswer(
            conversationId = payload.conversationId,
            messageId = payload.assistantMessageId,
            content = "",
            createdAt = now,
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

    private suspend fun onDone(
        conversationId: String,
        messageId: String,
        content: String,
    ) {
        streamingAnswer.value = null
        localDataSource.saveMessage(
            conversationId = conversationId,
            message = ChatMessageModel(
                id = messageId,
                role = ChatRoleModel.ASSISTANT,
                content = content,
                isStreaming = false,
                isFailed = false,
                createdAt = Clock.System.now(),
            ),
        )
        touchConversation(conversationId)
    }

    private suspend fun onTitleGenerated(
        conversationId: String,
        title: String,
    ) {
        val conversation = findConversation(conversationId) ?: return
        localDataSource.saveConversation(conversation.copy(title = title))
    }

    private suspend fun touchConversation(conversationId: String) {
        val conversation = findConversation(conversationId) ?: return
        localDataSource.saveConversation(conversation.copy(updatedAt = Clock.System.now()))
    }

    private suspend fun findConversation(conversationId: String): ChatConversationModel? = localDataSource
        .observeConversations()
        .first()
        .firstOrNull { it.id == conversationId }

    private suspend fun discardPendingAnswer(pending: PendingAnswer) {
        streamingAnswer.value = null
        suspendRunCatching {
            refreshConversations()
            refreshMessages(pending.conversationId)
        }.onFailure { error -> Logger.e(error) { "Failed to reconcile the thread after a failed answer" } }
    }

    override suspend fun renameConversation(
        conversationId: String,
        title: String,
    ) {
        conversationsDataSource.rename(
            conversationId = conversationId,
            title = title,
        )
        val conversation = findConversation(conversationId) ?: return
        localDataSource.saveConversation(conversation.copy(title = title))
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversationsDataSource.delete(conversationId)
        localDataSource.deleteConversation(conversationId)
        draftDataSource.deleteDraft(conversationId)
    }

    override fun observeDraft(threadKey: String): Flow<String> = draftDataSource.observeDraft(threadKey)

    override suspend fun saveDraft(draft: PendingDraftModel) {
        draftDataSource.saveDraft(draft)
    }

    private suspend fun mapFailure(
        throwable: Throwable,
        pending: PendingAnswer?,
        conversationId: String?,
    ): Throwable {
        pending?.let { discardPendingAnswer(it) }
        streamingAnswer.value = null
        throwable.getRateLimitRetrySeconds()?.let { seconds -> return ChatRateLimitedException(seconds) }
        if (throwable.isLimitReached()) return ChatLimitReachedException()
        if (conversationId != null && throwable.isConversationGone()) {
            localDataSource.deleteConversation(conversationId)
            draftDataSource.deleteDraft(conversationId)
            return ChatConversationGoneException()
        }
        Logger.e(throwable) { "Failed to send the chat message" }
        return throwable
    }

    private fun Throwable.getStatusCode(): Int? = when (this) {
        is RestException -> statusCode
        is SSEClientException -> response?.status?.value
        else -> null
    }

    private fun Throwable.isLimitReached(): Boolean = getStatusCode() == LIMIT_EXCEEDED_STATUS

    private fun Throwable.isConversationGone(): Boolean = getStatusCode() == CONVERSATION_GONE_STATUS

    private suspend fun Throwable.getRateLimitRetrySeconds(): Int? {
        if (getStatusCode() != RATE_LIMITED_STATUS) return null
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
        const val CONVERSATION_GONE_STATUS = 404
        const val RATE_LIMITED_STATUS = 429
        const val DEFAULT_RETRY_SECONDS = 30
    }
}
