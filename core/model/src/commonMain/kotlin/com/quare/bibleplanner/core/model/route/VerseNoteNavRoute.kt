package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/** A null [noteId] opens the editor on a new note for the passage. */
@Serializable
data class VerseNoteNavRoute(
    val bookId: String,
    val chapterNumber: Int,
    val verseNumbers: List<Int>,
    val noteId: String?,
) : NavRoute
