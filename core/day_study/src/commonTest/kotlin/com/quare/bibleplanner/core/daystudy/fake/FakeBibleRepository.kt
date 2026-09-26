package com.quare.bibleplanner.core.daystudy.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeBibleRepository : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = error("unused")

    override fun getSelectedVersionIdFlow(): Flow<String> = flowOf("ACF")

    override suspend fun setSelectedVersionId(id: String) = error("unused")
}
