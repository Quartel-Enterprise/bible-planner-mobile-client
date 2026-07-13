package com.quare.bibleplanner.feature.books.presentation.model

internal fun BookLayoutFormat.toAnalyticsValue(): String = when (this) {
    BookLayoutFormat.List -> "list"
    BookLayoutFormat.Grid -> "grid"
}
