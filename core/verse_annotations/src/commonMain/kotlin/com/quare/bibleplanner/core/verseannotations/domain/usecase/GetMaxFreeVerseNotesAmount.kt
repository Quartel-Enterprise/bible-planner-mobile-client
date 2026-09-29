package com.quare.bibleplanner.core.verseannotations.domain.usecase

fun interface GetMaxFreeVerseNotesAmount {
    suspend operator fun invoke(): Int
}
