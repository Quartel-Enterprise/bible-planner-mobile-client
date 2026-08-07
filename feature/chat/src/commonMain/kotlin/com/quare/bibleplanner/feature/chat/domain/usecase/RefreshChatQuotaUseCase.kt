package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class RefreshChatQuotaUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(): Result<Unit> = suspendRunCatching { repository.refreshQuota() }
}
