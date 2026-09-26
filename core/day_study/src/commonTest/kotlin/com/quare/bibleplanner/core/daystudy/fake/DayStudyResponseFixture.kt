package com.quare.bibleplanner.core.daystudy.fake

import com.quare.bibleplanner.core.daystudy.data.dto.ChapterSummaryDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyContentDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyResponseDto
import com.quare.bibleplanner.core.daystudy.data.dto.FactDto
import com.quare.bibleplanner.core.daystudy.data.dto.HistoricalContextDto
import com.quare.bibleplanner.core.daystudy.data.dto.QaDto
import com.quare.bibleplanner.core.daystudy.domain.model.ChapterSummaryModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.FactModel
import com.quare.bibleplanner.core.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.core.daystudy.domain.model.QaModel

internal fun dayStudyResponse(cacheToken: String): DayStudyResponseDto = DayStudyResponseDto(
    content = DayStudyContentDto(
        passageLabel = "Genesis 1-2",
        overview = "Creation and the garden",
        chapterSummaries = listOf(
            ChapterSummaryDto(
                title = "Genesis 1",
                body = "Six days of creation",
            ),
            ChapterSummaryDto(
                title = "Genesis 2",
                body = "The garden of Eden",
            ),
        ),
        takeaways = listOf("God creates with purpose", "Rest is holy"),
        context = HistoricalContextDto(
            body = "Written for Israel in the wilderness",
            facts = listOf(
                FactDto(
                    label = "Author",
                    value = "Moses",
                ),
                FactDto(
                    label = "Period",
                    value = "Bronze Age",
                ),
            ),
        ),
        commonQuestions = listOf(
            QaDto(
                question = "Why six days?",
                answer = "A pattern for work and rest",
            ),
        ),
    ),
    model = "model-x",
    promptVersion = 4,
    updatedAt = "2026-09-01T10:00:00Z",
    isPro = false,
    clientCacheToken = cacheToken,
)

internal val dayStudyModel = DayStudyModel(
    passageLabel = "Genesis 1-2",
    overview = "Creation and the garden",
    chapterSummaries = listOf(
        ChapterSummaryModel(
            title = "Genesis 1",
            body = "Six days of creation",
        ),
        ChapterSummaryModel(
            title = "Genesis 2",
            body = "The garden of Eden",
        ),
    ),
    takeaways = listOf("God creates with purpose", "Rest is holy"),
    context = HistoricalContextModel(
        body = "Written for Israel in the wilderness",
        facts = listOf(
            FactModel(
                label = "Author",
                value = "Moses",
            ),
            FactModel(
                label = "Period",
                value = "Bronze Age",
            ),
        ),
    ),
    commonQuestions = listOf(
        QaModel(
            question = "Why six days?",
            answer = "A pattern for work and rest",
        ),
    ),
)
