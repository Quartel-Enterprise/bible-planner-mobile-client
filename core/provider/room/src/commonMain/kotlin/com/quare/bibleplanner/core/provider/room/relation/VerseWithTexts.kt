package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.VerseEntity
import com.quare.bibleplanner.core.provider.room.entity.VerseTextEntity

data class VerseWithTexts(
    @Embedded
    val verse: VerseEntity,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["verseId"],
    )
    val texts: List<VerseTextEntity>,
)
