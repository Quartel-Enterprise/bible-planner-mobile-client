package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class ReadNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val isChapterRead: Boolean,
    val isFromBookDetails: Boolean,
) : NavKey
