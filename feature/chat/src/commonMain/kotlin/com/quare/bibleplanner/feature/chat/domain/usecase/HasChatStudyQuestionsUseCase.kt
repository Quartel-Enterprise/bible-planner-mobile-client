package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class HasChatStudyQuestionsUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(planDay: ChatPlanDayModel): Boolean = repository.hasStudyQuestions(planDay)
}
