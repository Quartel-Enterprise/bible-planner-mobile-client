package com.quare.bibleplanner.core.books.domain.model

data class VersesShareContentModel(
    val text: String,
    val reference: String,
    val versionAbbreviation: String,
) {
    val shareText: String
        get() = "$reference $versionAbbreviation\n$text"
}
