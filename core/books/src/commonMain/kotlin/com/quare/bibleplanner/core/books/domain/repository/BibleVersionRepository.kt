package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import kotlinx.coroutines.flow.Flow

interface BibleVersionRepository {
    fun getBibleVersionsFlow(): Flow<List<BibleVersionModel>>

    fun getSelectedVersionAbbreviationFlow(): Flow<String>

    suspend fun setSelectedVersionId(id: String)
}
