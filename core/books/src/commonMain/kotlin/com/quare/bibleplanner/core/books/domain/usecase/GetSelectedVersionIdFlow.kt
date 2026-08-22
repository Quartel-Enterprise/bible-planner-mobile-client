package com.quare.bibleplanner.core.books.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface GetSelectedVersionIdFlow {
    operator fun invoke(): Flow<String>
}
