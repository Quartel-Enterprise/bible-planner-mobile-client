package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveChatQuotaUseCase(
    private val repository: ChatRepository,
) {
    operator fun invoke(): Flow<ChatQuotaModel?> = repository.observeQuota()
}
