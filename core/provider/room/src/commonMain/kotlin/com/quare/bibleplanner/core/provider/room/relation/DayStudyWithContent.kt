package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.DayStudyChapterSummaryEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyFactEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyQuestionEntity
import com.quare.bibleplanner.core.provider.room.entity.DayStudyTakeawayEntity

data class DayStudyWithContent(
    @Embedded val study: DayStudyEntity,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val chapterSummaries: List<DayStudyChapterSummaryEntity>,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val takeaways: List<DayStudyTakeawayEntity>,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val facts: List<DayStudyFactEntity>,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val questions: List<DayStudyQuestionEntity>,
)
