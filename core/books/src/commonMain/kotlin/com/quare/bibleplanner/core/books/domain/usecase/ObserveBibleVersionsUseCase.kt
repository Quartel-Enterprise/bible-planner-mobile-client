package com.quare.bibleplanner.core.books.domain.usecase

fun interface ObserveBibleVersionsUseCase {
    suspend operator fun invoke()
}
