package com.quare.bibleplanner.core.books.domain.repository

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import kotlinx.coroutines.flow.Flow

interface BibleRepository {
    fun getBiblesFlow(): Flow<List<BibleModel>>

    fun getSelectedVersionIdFlow(): Flow<String>

    suspend fun setSelectedVersionId(id: String)
}
