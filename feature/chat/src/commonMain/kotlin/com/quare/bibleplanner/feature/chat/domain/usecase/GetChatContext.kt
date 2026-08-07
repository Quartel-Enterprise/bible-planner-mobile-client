package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel

/** Resolves the reading a chat was opened from into the context sent when creating the conversation. */
fun interface GetChatContext {
    suspend operator fun invoke(dayRoute: DayNavRoute?): ChatContextModel?
}
