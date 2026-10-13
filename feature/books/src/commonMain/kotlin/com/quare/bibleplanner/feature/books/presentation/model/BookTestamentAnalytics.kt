package com.quare.bibleplanner.feature.books.presentation.model

internal fun BookTestamentFilter.toAnalyticsValue(): String = when (this) {
    BookTestamentFilter.All -> "all"
    BookTestamentFilter.OldTestament -> "old"
    BookTestamentFilter.NewTestament -> "new"
}
