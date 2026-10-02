package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel

fun interface GetChatContext {
    suspend operator fun invoke(route: ChatNavRoute): ChatContextModel?
}
