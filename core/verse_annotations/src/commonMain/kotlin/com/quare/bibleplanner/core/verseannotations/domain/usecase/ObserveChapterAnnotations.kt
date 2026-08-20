package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.verseannotations.domain.model.ChapterAnnotations
import kotlinx.coroutines.flow.Flow

fun interface ObserveChapterAnnotations {
    operator fun invoke(chapter: ChapterRef): Flow<ChapterAnnotations>
}
