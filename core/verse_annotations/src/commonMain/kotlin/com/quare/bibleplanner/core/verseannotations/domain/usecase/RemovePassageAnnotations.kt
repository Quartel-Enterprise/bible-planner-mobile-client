package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage

fun interface RemovePassageAnnotations {
    suspend operator fun invoke(passage: AnnotatedPassage)
}
