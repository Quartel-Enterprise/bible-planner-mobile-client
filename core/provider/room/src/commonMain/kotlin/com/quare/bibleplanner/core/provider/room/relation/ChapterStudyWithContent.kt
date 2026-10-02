package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyCrossReferenceEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyNameEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyOutlineSectionEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterStudyQuestionEntity

data class ChapterStudyWithContent(
    @Embedded val study: ChapterStudyEntity,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val outlineSections: List<ChapterStudyOutlineSectionEntity>,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val names: List<ChapterStudyNameEntity>,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val crossReferences: List<ChapterStudyCrossReferenceEntity>,
    @Relation(parentColumns = ["cacheKey"], entityColumns = ["cacheKey"])
    val questions: List<ChapterStudyQuestionEntity>,
)
