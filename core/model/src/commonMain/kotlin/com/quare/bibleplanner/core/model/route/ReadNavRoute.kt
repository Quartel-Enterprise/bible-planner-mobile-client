package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ReadNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val isChapterRead: Boolean,
)
