package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import kotlinx.coroutines.flow.Flow

internal interface ChatRealtimeDataSource {
    fun observeConnected(): Flow<Unit>

    fun observeConversations(userId: String): Flow<ChatRemoteChange>

    fun observeMessages(userId: String): Flow<ChatRemoteChange>
}
