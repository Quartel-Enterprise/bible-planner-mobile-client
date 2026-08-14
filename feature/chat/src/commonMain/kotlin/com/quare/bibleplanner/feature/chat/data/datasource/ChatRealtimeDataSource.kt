package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.model.ChatRemoteChange
import kotlinx.coroutines.flow.Flow

internal interface ChatRealtimeDataSource {
    /**
     * Every transition into CONNECTED — cold start and each reconnection. It is the cue to pull a
     * fresh snapshot, since anything that changed while the socket was down was never delivered.
     */
    fun observeConnected(): Flow<Unit>

    fun observeConversations(userId: String): Flow<ChatRemoteChange>

    fun observeMessages(userId: String): Flow<ChatRemoteChange>
}
