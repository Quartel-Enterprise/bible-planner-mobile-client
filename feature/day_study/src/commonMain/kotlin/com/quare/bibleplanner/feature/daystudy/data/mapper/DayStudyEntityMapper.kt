package com.quare.bibleplanner.feature.daystudy.data.mapper

import com.quare.bibleplanner.core.provider.room.entity.DayStudyChapterSummaryEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyFactEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyTakeawayEntity
import com.quare.bibleplanner.core.provider.room.relation.DayStudyWithContent
import com.quare.bibleplanner.feature.daystudy.data.dto.DayStudyResponseDto
import com.quare.bibleplanner.feature.daystudy.domain.model.ChapterSummaryModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.FactModel
import com.quare.bibleplanner.feature.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.feature.daystudy.domain.model.QaModel

internal class DayStudyEntityMapper {
    fun mapToEntities(
        cacheKey: String,
        response: DayStudyResponseDto,
    ): DayStudyWithContent = DayStudyWithContent(
        study = DayStudyEntity(
            cacheKey = cacheKey,
            passageLabel = response.content.passageLabel,
            overview = response.content.overview,
            contextBody = response.content.context.body,
            model = response.model,
            promptVersion = response.promptVersion,
            updatedAt = response.updatedAt,
            cacheToken = response.clientCacheToken,
        ),
        chapterSummaries = response.content.chapterSummaries.mapIndexed { index, chapterSummary ->
            DayStudyChapterSummaryEntity(
                cacheKey = cacheKey,
                position = index,
                title = chapterSummary.title,
                body = chapterSummary.body,
            )
        },
        takeaways = response.content.takeaways.mapIndexed { index, takeaway ->
            DayStudyTakeawayEntity(
                cacheKey = cacheKey,
                position = index,
                text = takeaway,
            )
        },
        facts = response.content.context.facts.mapIndexed { index, fact ->
            DayStudyFactEntity(
                cacheKey = cacheKey,
                position = index,
                label = fact.label,
                value = fact.value,
            )
        },
        questions = response.content.commonQuestions.mapIndexed { index, question ->
            DayStudyQuestionEntity(
                cacheKey = cacheKey,
                position = index,
                question = question.question,
                answer = question.answer,
            )
        },
    )

    fun mapToDomain(content: DayStudyWithContent): DayStudyModel = DayStudyModel(
        passageLabel = content.study.passageLabel,
        overview = content.study.overview,
        chapterSummaries = content.chapterSummaries
            .sortedBy(DayStudyChapterSummaryEntity::position)
            .map { entity ->
                ChapterSummaryModel(
                    title = entity.title,
                    body = entity.body,
                )
            },
        takeaways = content.takeaways
            .sortedBy(DayStudyTakeawayEntity::position)
            .map(DayStudyTakeawayEntity::text),
        context = HistoricalContextModel(
            body = content.study.contextBody,
            facts = content.facts
                .sortedBy(DayStudyFactEntity::position)
                .map { entity ->
                    FactModel(
                        label = entity.label,
                        value = entity.value,
                    )
                },
        ),
        commonQuestions = content.questions
            .sortedBy(DayStudyQuestionEntity::position)
            .map { entity ->
                QaModel(
                    question = entity.question,
                    answer = entity.answer,
                )
            },
    )
}
