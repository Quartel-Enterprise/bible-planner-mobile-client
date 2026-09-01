package com.quare.bibleplanner.feature.chat.domain.model

import com.quare.bibleplanner.core.model.plan.PassageModel

data class ChatContextModel(
    val label: String,
    val passages: List<PassageModel>,
    val planDay: ChatPlanDayModel?,
)
