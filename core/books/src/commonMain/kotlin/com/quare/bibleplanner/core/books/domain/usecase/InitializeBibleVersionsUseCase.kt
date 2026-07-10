package com.quare.bibleplanner.core.books.domain.usecase

fun interface InitializeBibleVersionsUseCase {
    suspend operator fun invoke()
}
