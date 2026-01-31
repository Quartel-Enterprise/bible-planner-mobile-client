package com.quare.bibleplanner.feature.bibleversion.domain.repository

import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersion
import kotlinx.coroutines.flow.Flow

interface BibleVersionRepository {
    fun getBibleVersions(): Flow<List<BibleVersion>>
}
