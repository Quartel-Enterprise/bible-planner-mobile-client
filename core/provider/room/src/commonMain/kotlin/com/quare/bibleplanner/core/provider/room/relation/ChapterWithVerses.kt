package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity

// Why: stops at VerseEntity, not VerseWithTexts: pulling text in would carry every
// version's text and observe verse_texts, re-materialising the Bible on every
// download write.
data class ChapterWithVerses(
    @Embedded
    val chapter: ChapterEntity,
    @Relation(
        entity = VerseEntity::class,
        parentColumns = ["id"],
        entityColumns = ["chapterId"],
    )
    val verses: List<VerseEntity>,
)
