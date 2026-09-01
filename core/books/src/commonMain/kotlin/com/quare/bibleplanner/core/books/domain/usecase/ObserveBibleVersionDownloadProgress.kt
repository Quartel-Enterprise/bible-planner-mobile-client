package com.quare.bibleplanner.core.books.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveBibleVersionDownloadProgress {
    operator fun invoke(versionId: String): Flow<Float>
}
