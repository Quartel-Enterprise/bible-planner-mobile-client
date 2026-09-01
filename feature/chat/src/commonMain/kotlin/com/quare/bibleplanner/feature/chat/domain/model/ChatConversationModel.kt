package com.quare.bibleplanner.feature.chat.domain.model

import kotlin.time.Instant

data class ChatConversationModel(
    val id: String,
    val title: String,
    val preview: String?,
    val contextLabel: String?,
    val planDay: ChatPlanDayModel?,
    val updatedAt: Instant,
)
