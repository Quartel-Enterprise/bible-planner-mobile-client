package com.quare.bibleplanner.core.books.domain.model

/**
 * A selection rendered for sharing: [text] is the verse text (verse numbers included when the
 * selection spans more than one verse), [reference] the passage label and [versionName] the bible
 * version the text was taken from.
 */
data class VersesShareContentModel(
    val text: String,
    val reference: String,
    val versionName: String,
)
