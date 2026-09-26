package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeBibleRepository(
    initialBibles: List<BibleModel> = emptyList(),
) : BibleRepository {
    val bibles = MutableStateFlow(initialBibles)
    val selectedVersionIds = mutableListOf<String>()

    override fun getBiblesFlow(): Flow<List<BibleModel>> = bibles

    override fun getSelectedVersionIdFlow(): Flow<String> = error("Unexpected call")

    override suspend fun setSelectedVersionId(id: String) {
        selectedVersionIds += id
    }
}
