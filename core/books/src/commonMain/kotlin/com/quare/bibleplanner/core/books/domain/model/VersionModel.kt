package com.quare.bibleplanner.core.books.domain.model

import com.quare.bibleplanner.core.utils.locale.Language

data class VersionModel(
    val id: String,
    val name: String,
    val language: Language,
)
