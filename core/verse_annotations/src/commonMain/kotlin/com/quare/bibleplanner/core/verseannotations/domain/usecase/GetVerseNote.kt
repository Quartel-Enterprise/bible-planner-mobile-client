package com.quare.bibleplanner.core.verseannotations.domain.usecase

import com.quare.bibleplanner.core.verseannotations.domain.model.VerseNote

fun interface GetVerseNote {
    suspend operator fun invoke(noteId: String): VerseNote?
}
