package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity

/**
 * Deliberately stops at [VerseEntity] instead of [VerseWithTexts]: nothing reading a whole book
 * needs the verse text, and pulling it in would make every query over this relation carry every
 * downloaded version's text — and, worse, make the relation observe `verse_texts`, so a Bible
 * download would re-materialise the whole Bible on every write.
 */
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
