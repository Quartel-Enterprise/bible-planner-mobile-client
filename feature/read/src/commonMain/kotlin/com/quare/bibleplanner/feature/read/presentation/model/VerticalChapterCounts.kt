package com.quare.bibleplanner.feature.read.presentation.model

/** How many chapters vertical reading has pulled in on each side of the chapter the route opened. */
internal data class VerticalChapterCounts(
    val prepended: Int,
    val appended: Int,
)
