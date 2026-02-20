package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.BibleSelectionModel
import kotlinx.coroutines.flow.Flow

interface BibleVersionRepository {
    fun getSelectableBibleVersions(): Flow<List<BibleSelectionModel>>

    fun getSelectedVersionAbbreviationFlow(): Flow<String>

    suspend fun setSelectedVersionId(id: String)
}
