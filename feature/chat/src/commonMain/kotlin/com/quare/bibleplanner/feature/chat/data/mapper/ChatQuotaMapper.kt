package com.quare.bibleplanner.feature.chat.data.mapper

import com.quare.bibleplanner.feature.chat.data.dto.ChatDoneDto
import com.quare.bibleplanner.feature.chat.data.dto.ChatStatusDto
import com.quare.bibleplanner.feature.chat.domain.model.ChatQuotaModel

internal class ChatQuotaMapper {
    fun map(dto: ChatStatusDto): ChatQuotaModel = ChatQuotaModel(
        usedCount = dto.usedCount,
        freeLimit = dto.freeLimit,
        isPro = dto.isPro,
    )

    fun map(dto: ChatDoneDto): ChatQuotaModel = ChatQuotaModel(
        usedCount = dto.usedCount,
        freeLimit = dto.freeLimit,
        isPro = dto.isPro,
    )
}
