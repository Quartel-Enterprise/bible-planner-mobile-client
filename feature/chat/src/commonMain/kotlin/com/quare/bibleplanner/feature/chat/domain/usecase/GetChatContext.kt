package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel

fun interface GetChatContext {
    suspend operator fun invoke(dayRoute: DayNavRoute?): ChatContextModel?
}
