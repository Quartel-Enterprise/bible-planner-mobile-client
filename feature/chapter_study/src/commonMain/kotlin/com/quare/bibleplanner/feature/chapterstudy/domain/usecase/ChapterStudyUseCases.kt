package com.quare.bibleplanner.feature.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.FindCachedChapterStudy
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.RefreshChapterStudyCache
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected

internal data class ChapterStudyUseCases(
    val findCachedStudy: FindCachedChapterStudy,
    val refreshCache: RefreshChapterStudyCache,
    val getVersesShareContent: GetVersesShareContent,
    val isWholeChapterRead: IsWholeChapterRead,
    val isConnected: IsConnected,
    val observeIsProUser: ObserveIsProUser,
)
