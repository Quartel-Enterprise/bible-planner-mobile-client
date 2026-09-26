package com.quare.bibleplanner.feature.chat.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeBibleRepository(
    private val selectedVersionId: String,
) : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = error("unused")

    override fun getSelectedVersionIdFlow(): Flow<String> = flowOf(selectedVersionId)

    override suspend fun setSelectedVersionId(id: String) = error("unused")
}
