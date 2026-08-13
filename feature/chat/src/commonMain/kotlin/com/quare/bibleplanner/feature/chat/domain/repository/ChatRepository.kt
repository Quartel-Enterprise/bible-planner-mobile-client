package com.quare.bibleplanner.feature.chat.domain.repository

import com.quare.bibleplanner.feature.chat.domain.model.ChatConversationModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendEventModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatSendRequestModel
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeConversations(): Flow<List<ChatConversationModel>>

    fun observeMessages(conversationId: String): Flow<List<ChatMessageModel>>

    fun observeQuota(): Flow<ChatQuotaModel?>

    /** Runs until cancelled, applying the user's remote changes (other devices) to the session. */
    suspend fun syncRemoteChanges()

    suspend fun refreshConversations()

    suspend fun refreshMessages(conversationId: String)

    suspend fun refreshQuota()

    /** Streams one answer, growing the assistant message in the session as fragments arrive. */
    fun sendMessage(request: ChatSendRequestModel): Flow<ChatSendEventModel>

    suspend fun renameConversation(
        conversationId: String,
        title: String,
    )

    suspend fun deleteConversation(conversationId: String)

    /**
     * Records that this day's chat was entered through the study's questions, so the day's own
     * button offers them too from then on: they belong to the thread, not to the door taken.
     */
    suspend fun rememberStudyQuestions(planDay: ChatPlanDayModel)

    /** The composer draft of one thread, empty when there is none. */
    fun observeDraft(threadKey: String): Flow<String>

    suspend fun saveDraft(
        threadKey: String,
        content: String,
    )

    suspend fun hasStudyQuestions(planDay: ChatPlanDayModel): Boolean
}
