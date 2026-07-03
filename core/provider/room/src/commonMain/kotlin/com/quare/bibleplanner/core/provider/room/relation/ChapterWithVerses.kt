package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity

data class ChapterWithVerses(
    @Embedded
    val chapter: ChapterEntity,
    @Relation(
        entity = VerseEntity::class,
        parentColumns = ["id"],
        entityColumns = ["chapterId"],
    )
    val verses: List<VerseWithTexts>,
)
