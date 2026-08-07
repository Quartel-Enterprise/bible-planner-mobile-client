package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatConversationTitleUpdateDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

internal class ChatConversationsRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) {
    // RLS scopes every query to the caller, so no user filter is needed here.
    suspend fun fetchAll(): List<ChatConversationDto> = supabaseClient
        .from(TABLE)
        .select {
            order(
                column = UPDATED_AT_COLUMN,
                order = Order.DESCENDING,
            )
        }.decodeList()

    suspend fun rename(
        conversationId: String,
        title: String,
    ) {
        supabaseClient.from(TABLE).update(ChatConversationTitleUpdateDto(title)) {
            filter { eq(ID_COLUMN, conversationId) }
        }
    }

    suspend fun delete(conversationId: String) {
        supabaseClient.from(TABLE).delete {
            filter { eq(ID_COLUMN, conversationId) }
        }
    }

    private companion object {
        const val TABLE = "ai_chat_conversations"
        const val ID_COLUMN = "id"
        const val UPDATED_AT_COLUMN = "updated_at"
    }
}
