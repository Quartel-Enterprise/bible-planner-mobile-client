package com.quare.bibleplanner.core.verseannotations.domain.usecase

fun interface ShouldBlockAddVerseNote {
    suspend operator fun invoke(): Boolean
}
