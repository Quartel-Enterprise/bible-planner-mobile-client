package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import kotlinx.coroutines.flow.Flow

internal interface ChatRealtimeDataSource {
    // Why: emits on every transition into CONNECTED; changes made while the socket was
    // down are never delivered, so this is the cue to refetch a snapshot.
    fun observeConnected(): Flow<Unit>

    fun observeConversations(userId: String): Flow<ChatRemoteChange>

    fun observeMessages(userId: String): Flow<ChatRemoteChange>
}
