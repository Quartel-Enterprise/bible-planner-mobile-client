package com.quare.bibleplanner.feature.profile.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedBibleFlowUseCase
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetSelectedVersionDownloadedChaptersFlowUseCase(
    private val getSelectedBible: GetSelectedBibleFlowUseCase,
    private val verseDao: VerseDao,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Int> = getSelectedBible().flatMapLatest { bible ->
        bible?.version?.id?.let(verseDao::countChaptersWithVersesByVersionFlow) ?: flowOf(0)
    }
}
