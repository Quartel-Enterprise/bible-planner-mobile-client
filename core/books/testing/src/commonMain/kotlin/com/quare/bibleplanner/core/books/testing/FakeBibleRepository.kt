package com.quare.bibleplanner.core.books.testing

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBibleRepository(
    bibles: List<BibleModel>,
    selectedVersionId: String,
) : BibleRepository {
    val bibles = MutableStateFlow(bibles)
    val selectedVersionId = MutableStateFlow(selectedVersionId)
    val selectedVersionIds = mutableListOf<String>()

    override fun getBiblesFlow(): Flow<List<BibleModel>> = bibles

    override fun getSelectedVersionIdFlow(): Flow<String> = selectedVersionId

    override suspend fun setSelectedVersionId(id: String) {
        selectedVersionIds += id
        selectedVersionId.value = id
    }
}
