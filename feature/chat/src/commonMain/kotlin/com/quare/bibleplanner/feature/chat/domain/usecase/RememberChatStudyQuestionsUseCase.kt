package com.quare.bibleplanner.feature.chat.domain.usecase

import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository

class RememberChatStudyQuestionsUseCase(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(planDay: ChatPlanDayModel) {
        repository.rememberStudyQuestions(planDay)
    }
}
