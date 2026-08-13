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
import com.quare.bibleplanner.feature.chat.data.datasource.ChatStudyQuestionsLocalDataSource
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
import com.quare.bibleplanner.feature.chat.data.model.StreamingAnswer
import com.quare.bibleplanner.feature.chat.data.model.withStreamingAnswer
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
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

/**
 * Reads the chat from the local mirror (Room) and keeps that mirror in step with the server, so a
 * reader with no connection still has their conversations and can re-read them.
 *
 * Only what the server confirmed is cached. The answer being written right now lives in memory
 * instead, and is combined with the cached thread for display — a half-streamed answer must never
 * be persisted as if it were the final one. Everything is keyed by the id the server assigns, which
 * is what makes the streamed answer and its later Realtime insert land on the same message instead
 * of on two.
 */
internal class ChatRepositoryImpl(
    private val localDataSource: ChatLocalDataSource,
    private val studyQuestionsDataSource: ChatStudyQuestionsLocalDataSource,
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

    /**
     * Realtime only delivers what happens while the socket is up, so every reconnection re-pulls the
     * conversation list: that is what reconciles anything renamed or deleted elsewhere while this
     * device was offline.
     */
    private suspend fun pullOnConnected() {
        realtimeDataSource.observeConnected().collect {
            suspendRunCatching { refreshConversations() }
                .onFailure { error -> Logger.e(error) { "Failed to pull the chat snapshot" } }
        }
    }

    /**
     * Wipes the mirror only when a *different* account takes over without a logout in between —
     * signing out clears it through ClearChatLocalData instead. Two cases must not wipe: signing
     * into the same account, which is what every cold start looks like and is the whole point of
     * having a cache, and losing the session (an expired token would otherwise cost the reader
     * their offline history over a transient auth hiccup).
     */
    private suspend fun onUserChanged(userId: String?) {
        val previousUserId = currentUserId
        currentUserId = userId
        if (previousUserId == null || userId == null || previousUserId == userId) return
        quota.value = null
        localDataSource.deleteAll()
    }

    // One unusable change must not take the whole subscription down with it, so each is applied on
    // its own: the next event still arrives.
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

    /**
     * A message can arrive for a conversation this device has never seen — started on another
     * device, or created while the socket was down. Pulling the list first gives the message a
     * thread to belong to instead of dropping it.
     */
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
        }.catch { throwable -> throw mapFailure(throwable, pending) }
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

        // The model restarted from scratch: what was shown so far is no longer the answer.
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
        // The question is already persisted server-side at this point, so it belongs in the mirror
        // right away; only the answer is still provisional.
        localDataSource.saveMessage(
            conversationId = payload.conversationId,
            message = ChatMessageModel(
                id = payload.userMessageId,
                role = ChatRoleModel.USER,
                content = question,
                isStreaming = false,
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
                // Stamped now, not when the question was sent: an answer that carried the question's
                // own instant left the two tied, and the thread then ordered them by their random
                // ids — which is how an answer could end up above the question that asked for it.
                createdAt = Clock.System.now(),
            ),
        )
        // Answering is what moves a conversation to the top of the history, so the mirror follows.
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

    /**
     * What survives a failed generation is the server's to say: it drops the question when the
     * answer fails, but a connection that died mid-stream may have left the row standing. Dropping
     * it locally on a guess is what left the asking device without the question while every other
     * device still showed it, so the thread is re-read instead.
     */
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
    }

    override suspend fun rememberStudyQuestions(planDay: ChatPlanDayModel) {
        studyQuestionsDataSource.remember(planDay)
    }

    override suspend fun hasStudyQuestions(planDay: ChatPlanDayModel): Boolean =
        studyQuestionsDataSource.contains(planDay)

    override fun observeDraft(threadKey: String): Flow<String> = draftDataSource.observeDraft(threadKey)

    override suspend fun saveDraft(
        threadKey: String,
        content: String,
    ) {
        draftDataSource.saveDraft(
            threadKey = threadKey,
            content = content,
        )
    }

    private suspend fun mapFailure(
        throwable: Throwable,
        pending: PendingAnswer?,
    ): Throwable {
        pending?.let { discardPendingAnswer(it) }
        streamingAnswer.value = null
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
    }
}
