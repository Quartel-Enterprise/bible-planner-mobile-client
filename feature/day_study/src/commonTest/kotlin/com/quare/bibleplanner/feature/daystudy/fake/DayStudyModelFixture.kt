package com.quare.bibleplanner.feature.daystudy.fake

import com.quare.bibleplanner.core.daystudy.domain.model.ChapterSummaryModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.FactModel
import com.quare.bibleplanner.core.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.core.daystudy.domain.model.QaModel

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
