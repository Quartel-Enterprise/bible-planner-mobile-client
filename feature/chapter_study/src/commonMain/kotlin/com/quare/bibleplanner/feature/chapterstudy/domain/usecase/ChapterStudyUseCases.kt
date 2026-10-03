package com.quare.bibleplanner.feature.chapterstudy.domain.usecase

import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.FindCachedChapterStudy
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyAccess
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.GetChapterStudyQuota
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.RefreshChapterStudyCache
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.connectivity.domain.usecase.IsConnected
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId

internal data class ChapterStudyUseCases(
    val findCachedStudy: FindCachedChapterStudy,
    val refreshCache: RefreshChapterStudyCache,
    val getVersesShareContent: GetVersesShareContent,
    val isWholeChapterRead: IsWholeChapterRead,
    val isConnected: IsConnected,
    val observeIsProUser: ObserveIsProUser,
    val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    val getAccess: GetChapterStudyAccess,
    val getQuota: GetChapterStudyQuota,
)
