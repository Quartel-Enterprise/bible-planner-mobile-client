package com.quare.bibleplanner.core.provider.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.quare.bibleplanner.core.provider.room.entity.BookEntity
import com.quare.bibleplanner.core.provider.room.entity.ChapterEntity

data class BookWithChapters(
    @Embedded
    val book: BookEntity,
    @Relation(
        entity = ChapterEntity::class,
        parentColumn = "id",
        entityColumn = "bookId",
    )
    val chapters: List<ChapterWithVerses>,
)
