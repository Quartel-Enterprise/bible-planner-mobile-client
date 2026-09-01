package com.quare.bibleplanner.feature.chat.domain.repository

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

internal class FakeChatRepository : ChatRepository {
    val conversations: MutableStateFlow<List<ChatConversationModel>> = MutableStateFlow(emptyList())
    val messages: MutableStateFlow<Map<String, List<ChatMessageModel>>> = MutableStateFlow(emptyMap())
    val quota: MutableStateFlow<ChatQuotaModel?> = MutableStateFlow(null)

    val renamed: MutableList<Pair<String, String>> = mutableListOf()
    val deleted: MutableList<String> = mutableListOf()
    val loadedConversationIds: MutableList<String> = mutableListOf()
    val drafts: MutableStateFlow<Map<String, String>> = MutableStateFlow(emptyMap())
    var refreshedConversations: Int = 0
    var refreshedQuota: Int = 0

    override fun observeConversations(): Flow<List<ChatConversationModel>> = conversations

    override fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>> = messages
        .map { current -> current[conversationId].orEmpty() }

    override fun observeQuota(): Flow<ChatQuotaModel?> = quota

    override suspend fun syncRemoteChanges() = Unit

    override suspend fun refreshConversations() {
        refreshedConversations++
    }

    override suspend fun refreshMessages(conversationId: String) {
        loadedConversationIds += conversationId
    }

    override suspend fun refreshQuota() {
        refreshedQuota++
    }

    override fun sendMessage(request: ChatSendRequestModel): Flow<ChatSendEventModel> = emptyFlow()

    override suspend fun renameConversation(
        conversationId: String,
        title: String,
    ) {
        renamed += conversationId to title
    }

    override suspend fun deleteConversation(conversationId: String) {
        deleted += conversationId
    }

    override fun observeDraft(threadKey: String): Flow<String> = drafts
        .map { current -> current[threadKey].orEmpty() }

    override suspend fun saveDraft(draft: PendingDraftModel) {
        drafts.value = drafts.value + (draft.threadKey to draft.content)
    }
}
