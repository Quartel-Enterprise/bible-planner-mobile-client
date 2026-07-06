package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.feature.daystudy.data.dto.ChapterSummaryDto
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyContentDto
import com.quare.bibleplanner.feature.daystudy.data.dto.FactDto
import com.quare.bibleplanner.feature.daystudy.data.dto.QaDto
import com.quare.bibleplanner.feature.daystudy.domain.model.ChapterSummaryModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.FactModel
import com.quare.bibleplanner.feature.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.feature.daystudy.domain.model.QaModel

internal class DayStudyContentMapper {
    fun map(content: DayStudyContentDto): DayStudyModel = DayStudyModel(
        passageLabel = content.passageLabel,
        overview = content.overview,
        chapterSummaries = content.chapterSummaries.map(::mapChapterSummary),
        takeaways = content.takeaways,
        context = HistoricalContextModel(
            body = content.context.body,
            facts = content.context.facts.map(::mapFact),
        ),
        commonQuestions = content.commonQuestions.map(::mapQa),
    )

    private fun mapChapterSummary(dto: ChapterSummaryDto): ChapterSummaryModel = ChapterSummaryModel(
        title = dto.title,
        body = dto.body,
    )

    private fun mapFact(dto: FactDto): FactModel = FactModel(
        label = dto.label,
        value = dto.value,
    )

    private fun mapQa(dto: QaDto): QaModel = QaModel(
        question = dto.question,
        answer = dto.answer,
    )
}
