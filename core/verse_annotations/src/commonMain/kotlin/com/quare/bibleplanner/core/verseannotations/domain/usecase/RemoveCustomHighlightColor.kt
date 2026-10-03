package com.quare.bibleplanner.core.verseannotations.domain.usecase

fun interface RemoveCustomHighlightColor {
    suspend operator fun invoke(
        colorKey: String,
        shouldKeepHighlights: Boolean,
    )
}
