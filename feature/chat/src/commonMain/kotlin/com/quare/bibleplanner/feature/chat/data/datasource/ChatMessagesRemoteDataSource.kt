package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatMessageDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

internal class ChatMessagesRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun fetchByConversation(conversationId: String): List<ChatMessageDto> = supabaseClient
        .from(TABLE)
        .select {
            filter { eq(CONVERSATION_ID_COLUMN, conversationId) }
            order(
                column = CREATED_AT_COLUMN,
                order = Order.ASCENDING,
            )
        }.decodeList()

    private companion object {
        const val TABLE = "ai_chat_messages"
        const val CONVERSATION_ID_COLUMN = "conversation_id"
        const val CREATED_AT_COLUMN = "created_at"
    }
}
