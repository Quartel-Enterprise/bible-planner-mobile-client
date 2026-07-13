package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookTestament

internal fun BookTestament.toAnalyticsValue(): String = when (this) {
    BookTestament.OldTestament -> "old"
    BookTestament.NewTestament -> "new"
}
