package com.quare.bibleplanner.core.books.domain.model

import com.quare.bibleplanner.core.utils.locale.Language

data class VersionModel(
    val id: String,
    val name: String,
    val version: String,
    val language: Language,
    val chapters: Int,
    val size: Long?,
)
