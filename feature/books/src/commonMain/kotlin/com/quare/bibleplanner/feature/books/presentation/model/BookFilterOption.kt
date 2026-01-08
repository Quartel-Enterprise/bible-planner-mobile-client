package com.quare.bibleplanner.feature.books.presentation.model

import org.jetbrains.compose.resources.StringResource

data class BookFilterOption(
    val type: BookFilterType,
    val label: StringResource,
    val isSelected: Boolean,
)
