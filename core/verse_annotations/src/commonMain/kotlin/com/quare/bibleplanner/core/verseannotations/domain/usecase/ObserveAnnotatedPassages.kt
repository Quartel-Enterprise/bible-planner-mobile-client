package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage
import kotlinx.coroutines.flow.Flow

fun interface ObserveAnnotatedPassages {
    operator fun invoke(bibleVersionId: String): Flow<List<AnnotatedPassage>>
}
