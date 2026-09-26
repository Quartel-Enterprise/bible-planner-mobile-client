package com.quare.bibleplanner.feature.profile.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeBibleRepository(
    private val bibles: List<BibleModel>,
) : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(bibles)

    override fun getSelectedVersionIdFlow(): Flow<String> = error("unused")

    override suspend fun setSelectedVersionId(id: String) = error("unused")
}
