package com.quare.bibleplanner.feature.read.presentation.model

enum class VerseNoteMarkPosition {
    SINGLE,
    FIRST,
    MIDDLE,
    LAST,
    ;

    val hasIcon: Boolean
        get() = this == SINGLE || this == FIRST

    val isLinkedAbove: Boolean
        get() = this == MIDDLE || this == LAST

    val isLinkedBelow: Boolean
        get() = this == FIRST || this == MIDDLE
}
