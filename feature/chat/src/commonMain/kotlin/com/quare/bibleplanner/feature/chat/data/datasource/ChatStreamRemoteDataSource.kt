package com.quare.bibleplanner.feature.chat.data.datasource

import com.quare.bibleplanner.feature.chat.data.dto.AskChatRequestDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatStatusDto
import com.quare.bibleplanner.feature.chat.data.model.ChatStreamEvent
import kotlinx.coroutines.flow.Flow

internal interface ChatStreamRemoteDataSource {
    fun streamAnswer(request: AskChatRequestDto): Flow<ChatStreamEvent>

    suspend fun fetchStatus(): Result<ChatStatusDto>
}
