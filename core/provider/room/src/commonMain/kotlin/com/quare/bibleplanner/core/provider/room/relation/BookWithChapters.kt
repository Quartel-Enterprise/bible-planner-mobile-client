package com.quare.bibleplanner.core.provider.room.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity

data class BookWithChapters(
    @Embedded
    val book: BookEntity,
    @Relation(
        entity = ChapterEntity::class,
        parentColumns = ["id"],
        entityColumns = ["bookId"],
    )
    val chapters: List<ChapterWithVerses>,
)
