package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow

internal class FakeBibleRepository(
    private val bibles: Flow<List<BibleModel>>,
    private val selectedVersionId: Flow<String>,
) : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = bibles

    override fun getSelectedVersionIdFlow(): Flow<String> = selectedVersionId

    override suspend fun setSelectedVersionId(id: String) = error("unused")
}
