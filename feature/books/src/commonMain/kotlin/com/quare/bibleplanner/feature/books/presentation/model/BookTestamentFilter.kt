package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookTestament

enum class BookTestamentFilter(
    val testament: BookTestament?,
) {
    All(testament = null),
    OldTestament(testament = BookTestament.OldTestament),
    NewTestament(testament = BookTestament.NewTestament),
}
