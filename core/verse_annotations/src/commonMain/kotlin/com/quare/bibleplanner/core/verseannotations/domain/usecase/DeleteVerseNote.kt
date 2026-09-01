package com.quare.bibleplanner.core.verseannotations.domain.usecase

fun interface DeleteVerseNote {
    suspend operator fun invoke(noteId: String)
}
