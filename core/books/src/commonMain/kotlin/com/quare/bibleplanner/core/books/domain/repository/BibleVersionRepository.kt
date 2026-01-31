package com.quare.bibleplanner.core.books.domain.repository

import kotlinx.coroutines.flow.Flow

interface BibleVersionRepository {
    fun getSelectedVersionAbbreviationFlow(): Flow<String>

    suspend fun setSelectedVersionAbbreviation(abbreviation: String)
}
