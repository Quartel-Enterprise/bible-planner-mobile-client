package com.quare.bibleplanner.feature.chat.presentation.model

import kotlinx.datetime.Month

sealed interface ChatConversationBucket {
    data object Today : ChatConversationBucket

    data object Yesterday : ChatConversationBucket

    data object LastSevenDays : ChatConversationBucket

    data class InMonth(
        val month: Month,
        val year: Int,
    ) : ChatConversationBucket
}
