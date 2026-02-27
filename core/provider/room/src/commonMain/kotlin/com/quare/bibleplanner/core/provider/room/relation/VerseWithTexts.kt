package com.quare.bibleplanner.core.provider.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity

data class VerseWithTexts(
    @Embedded
    val verse: VerseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "verseId",
    )
    val texts: List<VerseTextEntity>,
)
