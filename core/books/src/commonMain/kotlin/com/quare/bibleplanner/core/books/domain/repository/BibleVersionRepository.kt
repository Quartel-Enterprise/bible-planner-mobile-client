package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import kotlinx.coroutines.flow.Flow

interface BibleVersionRepository {
    fun getBibleVersions(): Flow<List<BibleVersionModel>>

    fun getSelectedVersionAbbreviationFlow(): Flow<String>

    suspend fun setSelectedVersionAbbreviation(abbreviation: String)
}
