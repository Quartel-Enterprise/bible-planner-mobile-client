package com.quare.bibleplanner.feature.bibleversion.presentation.model

data class PendingBibleUpdateItem(
    val id: String,
    val name: String,
    val size: Long?,
    val isSelected: Boolean,
)
