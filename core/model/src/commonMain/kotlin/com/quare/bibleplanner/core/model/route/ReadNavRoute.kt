package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ReadNavRoute(
    val bookId: String,
    val chapterId: Long,
    val chapterNumber: Int,
)
