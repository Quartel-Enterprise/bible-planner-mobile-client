package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.AskChatRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatStatusDto
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeChatLocalDataSource : ChatLocalDataSource {
    val conversations: MutableStateFlow<List<ChatConversationModel>> = MutableStateFlow(emptyList())
    val messages: MutableStateFlow<Map<String, List<ChatMessageModel>>> = MutableStateFlow(emptyMap())

    override fun observeConversations(): Flow<List<ChatConversationModel>> = conversations

    override fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>> = messages
        .map { current -> current[conversationId].orEmpty() }

    override suspend fun replaceConversations(conversations: List<ChatConversationModel>) {
        this.conversations.value = conversations
    }

    override suspend fun saveConversation(conversation: ChatConversationModel) {
        conversations.value = conversations.value.filterNot { it.id == conversation.id } + conversation
    }

    override suspend fun replaceMessages(
        conversationId: String,
        messages: List<ChatMessageModel>,
    ) {
        this.messages.value = this.messages.value + (conversationId to messages)
    }

    override suspend fun saveMessage(
        conversationId: String,
        message: ChatMessageModel,
    ) {
        val thread = messages.value[conversationId].orEmpty().filterNot { it.id == message.id }
        messages.value = messages.value + (conversationId to thread + message)
    }

    override suspend fun deleteMessage(messageId: String) {
        messages.value = messages.value.mapValues { (_, thread) -> thread.filterNot { it.id == messageId } }
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversations.value = conversations.value.filterNot { it.id == conversationId }
        messages.value = messages.value - conversationId
    }

    override suspend fun deleteAll() {
        conversations.value = emptyList()
        messages.value = emptyMap()
    }
}

internal class FakeChatDraftLocalDataSource : ChatDraftLocalDataSource {
    val drafts: MutableStateFlow<Map<String, String>> = MutableStateFlow(emptyMap())

    override fun observeDraft(threadKey: String): Flow<String> = drafts
        .map { current -> current[threadKey].orEmpty() }

    override suspend fun saveDraft(draft: PendingDraftModel) {
        drafts.value = drafts.value + (draft.threadKey to draft.content)
    }

    override suspend fun deleteDraft(threadKey: String) {
        drafts.value = drafts.value - threadKey
    }
}

internal class FakeChatConversationsRemoteDataSource : ChatConversationsRemoteDataSource {
    var remoteConversations: List<ChatConversationDto> = emptyList()
    val renamed: MutableList<Pair<String, String>> = mutableListOf()
    val deleted: MutableList<String> = mutableListOf()
    var fetchCount: Int = 0

    override suspend fun fetchAll(): List<ChatConversationDto> {
        fetchCount++
        return remoteConversations
    }

    override suspend fun rename(
        conversationId: String,
        title: String,
    ) {
        renamed += conversationId to title
    }

    override suspend fun delete(conversationId: String) {
        deleted += conversationId
    }
}

internal class FakeChatMessagesRemoteDataSource : ChatMessagesRemoteDataSource {
    var remoteMessages: Map<String, List<ChatMessageDto>> = emptyMap()
    val fetchedConversationIds: MutableList<String> = mutableListOf()

    override suspend fun fetchByConversation(conversationId: String): List<ChatMessageDto> {
        fetchedConversationIds += conversationId
        return remoteMessages[conversationId].orEmpty()
    }
}

internal class FakeChatStreamRemoteDataSource : ChatStreamRemoteDataSource {
    private val events: MutableSharedFlow<Result<ChatStreamEvent>> = MutableSharedFlow()
    var status: ChatStatusDto = ChatStatusDto(
        usedCount = 0,
        freeLimit = 10,
        isPro = false,
    )
    val requests: MutableList<AskChatRequestDto> = mutableListOf()

    suspend fun emit(event: ChatStreamEvent) {
        events.emit(Result.success(event))
    }

    suspend fun fail(error: Throwable) {
        events.emit(Result.failure(error))
    }

    override fun streamAnswer(request: AskChatRequestDto): Flow<ChatStreamEvent> {
        requests += request
        return events.map { result -> result.getOrThrow() }
    }

    override suspend fun fetchStatus(): Result<ChatStatusDto> = Result.success(status)
}

internal class FakeChatRealtimeDataSource : ChatRealtimeDataSource {
    val connections: MutableSharedFlow<Unit> = MutableSharedFlow()
    val conversationChanges: MutableSharedFlow<ChatRemoteChange> = MutableSharedFlow()
    val messageChanges: MutableSharedFlow<ChatRemoteChange> = MutableSharedFlow()

    override fun observeConnected(): Flow<Unit> = connections

    override fun observeConversations(userId: String): Flow<ChatRemoteChange> = conversationChanges

    override fun observeMessages(userId: String): Flow<ChatRemoteChange> = messageChanges
}
