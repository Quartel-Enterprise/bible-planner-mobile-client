package com.quare.bibleplanner.android.feature.books

internal data class BooksMenuOption(
    val title: String,
    val isSelected: Boolean,
    val onClick: () -> Unit,
)
