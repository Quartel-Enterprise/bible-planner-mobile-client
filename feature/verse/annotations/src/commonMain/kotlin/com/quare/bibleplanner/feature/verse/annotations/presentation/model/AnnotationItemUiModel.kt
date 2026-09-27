package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.verseannotations.domain.model.AnnotatedPassage

internal data class AnnotationItemUiModel(
    val key: String,
    val passage: AnnotatedPassage,
    val text: String,
    val versionAbbreviation: String,
)
