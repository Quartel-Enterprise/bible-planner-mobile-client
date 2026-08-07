package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.books.util.getReadingLabel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.usecase.GetChatContext
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class GetChatContextUseCase(
    private val getDayPassages: GetDayPassagesForDayStudyUseCase,
) : GetChatContext {
    override suspend fun invoke(dayRoute: DayNavRoute?): ChatContextModel? {
        if (dayRoute == null) return null
        val passages = getDayPassages(
            weekNumber = dayRoute.weekNumber,
            dayNumber = dayRoute.dayNumber,
            readingPlanType = ReadingPlanType.valueOf(dayRoute.readingPlanType),
        ).filterNotNull().first()
        if (passages.isEmpty()) return null
        return ChatContextModel(
            label = passages.getReadingLabel(),
            passages = passages,
        )
    }
}
