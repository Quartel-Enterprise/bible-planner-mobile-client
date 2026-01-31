package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersion
import com.quare.bibleplanner.feature.bibleversion.domain.repository.BibleVersionRepository
import kotlinx.coroutines.flow.Flow

class GetBibleVersionsUseCase(
    private val repository: BibleVersionRepository,
) {
    operator fun invoke(): Flow<List<BibleVersion>> = repository.getBibleVersions()
}
