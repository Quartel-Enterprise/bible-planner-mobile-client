package com.quare.bibleplanner.core.verseannotations.domain.usecase

fun interface RemoveCustomHighlightColor {
    /**
     * Drops the colour from the palette, and when [shouldKeepHighlights] is false also clears every
     * highlight made with it.
     */
    suspend operator fun invoke(
        colorKey: String,
        shouldKeepHighlights: Boolean,
    )
}
