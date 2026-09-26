package com.quare.bibleplanner.core.books.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeBibleRepository(
    bibles: List<BibleModel>,
    selectedVersionId: String,
) : BibleRepository {
    val bibles = MutableStateFlow(bibles)
    private val selectedVersionId = MutableStateFlow(selectedVersionId)

    override fun getBiblesFlow(): Flow<List<BibleModel>> = bibles

    override fun getSelectedVersionIdFlow(): Flow<String> = selectedVersionId

    override suspend fun setSelectedVersionId(id: String) {
        selectedVersionId.value = id
    }
}
