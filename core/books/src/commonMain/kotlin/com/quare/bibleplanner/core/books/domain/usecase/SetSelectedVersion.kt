package com.quare.bibleplanner.core.books.domain.usecase

fun interface SetSelectedVersion {
    suspend operator fun invoke(versionId: String)
}
