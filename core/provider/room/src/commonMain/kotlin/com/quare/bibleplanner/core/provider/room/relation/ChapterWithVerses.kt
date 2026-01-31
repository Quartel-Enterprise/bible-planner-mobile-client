package com.quare.bibleplanner.core.provider.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity

data class ChapterWithVerses(
    @Embedded
    val chapter: ChapterEntity,
    @Relation(
        entity = VerseEntity::class,
        parentColumn = "id",
        entityColumn = "chapterId",
    )
    val verses: List<VerseWithTexts>,
)
