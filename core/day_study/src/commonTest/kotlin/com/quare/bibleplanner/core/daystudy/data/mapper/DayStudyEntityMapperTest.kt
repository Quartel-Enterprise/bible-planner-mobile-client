package com.quare.bibleplanner.core.daystudy.data.mapper

import com.quare.bibleplanner.core.daystudy.fake.dayStudyModel
import com.quare.bibleplanner.core.daystudy.fake.dayStudyResponse
import com.quare.bibleplanner.core.provider.room.entity.DayStudyChapterSummaryEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyFactEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyTakeawayEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyEntityMapperTest {
    private lateinit var mapper: DayStudyEntityMapper

    @BeforeTest
    fun setUp() {
        mapper = DayStudyEntityMapper()
    }

    @Test
    fun `GIVEN a study response WHEN mapping to entities THEN stores the study with positioned children`() {
        // When
        val content = mapper.mapToEntities(
            cacheKey = "key",
            response = dayStudyResponse(cacheToken = "token-1"),
        )

        // Then
        assertEquals(
            DayStudyEntity(
                cacheKey = "key",
                passageLabel = "Genesis 1-2",
                overview = "Creation and the garden",
                contextBody = "Written for Israel in the wilderness",
                model = "model-x",
                promptVersion = 4,
                updatedAt = "2026-09-01T10:00:00Z",
                cacheToken = "token-1",
            ),
            content.study,
        )
        assertEquals(
            listOf(
                DayStudyChapterSummaryEntity(
                    cacheKey = "key",
                    position = 0,
                    title = "Genesis 1",
                    body = "Six days of creation",
                ),
                DayStudyChapterSummaryEntity(
                    cacheKey = "key",
                    position = 1,
                    title = "Genesis 2",
                    body = "The garden of Eden",
                ),
            ),
            content.chapterSummaries,
        )
        assertEquals(
            listOf(
                DayStudyTakeawayEntity(
                    cacheKey = "key",
                    position = 0,
                    text = "God creates with purpose",
                ),
                DayStudyTakeawayEntity(
                    cacheKey = "key",
                    position = 1,
                    text = "Rest is holy",
                ),
            ),
            content.takeaways,
        )
        assertEquals(
            listOf(
                DayStudyFactEntity(
                    cacheKey = "key",
                    position = 0,
                    label = "Author",
                    value = "Moses",
                ),
                DayStudyFactEntity(
                    cacheKey = "key",
                    position = 1,
                    label = "Period",
                    value = "Bronze Age",
                ),
            ),
            content.facts,
        )
        assertEquals(
            listOf(
                DayStudyQuestionEntity(
                    cacheKey = "key",
                    position = 0,
                    question = "Why six days?",
                    answer = "A pattern for work and rest",
                ),
            ),
            content.questions,
        )
    }

    @Test
    fun `GIVEN stored entities out of order WHEN mapping to the domain THEN sorts every section by position`() {
        // Given
        val stored = mapper.mapToEntities(
            cacheKey = "key",
            response = dayStudyResponse(cacheToken = "token-1"),
        )
        val shuffled = stored.copy(
            chapterSummaries = stored.chapterSummaries.reversed(),
            takeaways = stored.takeaways.reversed(),
            facts = stored.facts.reversed(),
            questions = stored.questions.reversed(),
        )

        // When
        val study = mapper.mapToDomain(shuffled)

        // Then
        assertEquals(dayStudyModel, study)
    }
}
