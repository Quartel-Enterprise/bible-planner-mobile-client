package com.quare.bibleplanner.feature.verse.annotations.domain.model

import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage

internal data class AnnotationEntry(
    val passage: AnnotatedPassage,
    val text: String,
    val reference: String,
)
