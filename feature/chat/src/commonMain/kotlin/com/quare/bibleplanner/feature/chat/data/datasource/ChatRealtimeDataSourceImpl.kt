package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.core.provider.supabase.USER_ID_COLUMN
import com.quare.bibleplanner.core.provider.supabase.decodeUpsert
import com.quare.bibleplanner.core.provider.supabase.observeUserRows
import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatDeletedRowDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.decodeOldRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal class ChatRealtimeDataSourceImpl(
    private val realtime: Realtime,
) : ChatRealtimeDataSource {
    override fun observeConnected(): Flow<Unit> = realtime.status
        .filter { status -> status == Realtime.Status.CONNECTED }
        .map { }

    override fun observeConversations(userId: String): Flow<ChatRemoteChange> = realtime.observeUserRows(
        table = CONVERSATIONS_TABLE,
        userId = userId,
        userIdColumn = USER_ID_COLUMN,
        upserted = { action ->
            action.decodeUpsert<ChatConversationDto>()?.let(ChatRemoteChange::ConversationUpserted)
        },
        deleted = { action -> ChatRemoteChange.ConversationDeleted(action.decodeOldRecord<ChatDeletedRowDto>().id) },
    )

    override fun observeMessages(userId: String): Flow<ChatRemoteChange> = realtime.observeUserRows(
        table = MESSAGES_TABLE,
        userId = userId,
        userIdColumn = USER_ID_COLUMN,
        upserted = { action -> action.decodeUpsert<ChatMessageDto>()?.let(ChatRemoteChange::MessageUpserted) },
        deleted = { action -> ChatRemoteChange.MessageDeleted(action.decodeOldRecord<ChatDeletedRowDto>().id) },
    )

    private companion object {
        const val CONVERSATIONS_TABLE = "ai_chat_conversations"
        const val MESSAGES_TABLE = "ai_chat_messages"
    }
}
