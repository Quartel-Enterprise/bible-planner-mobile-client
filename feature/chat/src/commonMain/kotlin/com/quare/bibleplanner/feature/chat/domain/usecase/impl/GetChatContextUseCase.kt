package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.books.util.getReadingLabel
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.toDayNavRoute
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.domain.usecase.GetChatContext
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class GetChatContextUseCase(
    private val getDayPassages: GetDayPassagesForDayStudyUseCase,
) : GetChatContext {
    override suspend fun invoke(route: ChatNavRoute): ChatContextModel? {
        val dayRoute = route.toDayNavRoute()
        return if (dayRoute != null) getDayContext(dayRoute) else getChapterContext(route)
    }

    private suspend fun getDayContext(dayRoute: DayNavRoute): ChatContextModel? {
        val passages = getDayPassages(
            weekNumber = dayRoute.weekNumber,
            dayNumber = dayRoute.dayNumber,
            readingPlanType = ReadingPlanType.valueOf(dayRoute.readingPlanType),
        ).filterNotNull().first()
        if (passages.isEmpty()) return null
        return ChatContextModel(
            label = passages.getReadingLabel(),
            passages = passages,
            planDay = ChatPlanDayModel(
                dayNumber = dayRoute.dayNumber,
                weekNumber = dayRoute.weekNumber,
                readingPlanType = dayRoute.readingPlanType,
            ),
        )
    }

    private suspend fun getChapterContext(route: ChatNavRoute): ChatContextModel? {
        val bookId = route.bookId?.let(BookId::valueOf) ?: return null
        val chapterNumber = route.chapterNumber ?: return null
        val passages = listOf(
            PassageModel(
                bookId = bookId,
                chapters = listOf(
                    ChapterModel(
                        number = chapterNumber,
                        startVerse = null,
                        endVerse = null,
                        bookId = bookId,
                    ),
                ),
                isRead = false,
                chapterRanges = chapterNumber.toString(),
            ),
        )
        return ChatContextModel(
            label = passages.getReadingLabel(),
            passages = passages,
            planDay = null,
        )
    }
}
