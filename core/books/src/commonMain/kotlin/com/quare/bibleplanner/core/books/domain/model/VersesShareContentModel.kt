package com.quare.bibleplanner.core.books.domain.model

/**
 * A selection rendered for sharing: [text] is the verse text, one verse per line, [reference] the
 * passage label and [versionAbbreviation] the short name of the version it was taken from.
 */
data class VersesShareContentModel(
    val text: String,
    val reference: String,
    val versionAbbreviation: String,
) {
    /**
     * The passage as it is pasted elsewhere: the reference heads it, the way a citation is read out,
     * and the verses follow underneath.
     */
    val shareText: String
        get() = "$reference $versionAbbreviation\n$text"
}
