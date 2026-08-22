package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class ShareVerseNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
) : NavRoute
