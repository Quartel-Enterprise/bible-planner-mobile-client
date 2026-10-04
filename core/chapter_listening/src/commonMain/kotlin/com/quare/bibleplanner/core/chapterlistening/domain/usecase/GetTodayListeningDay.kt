package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel

fun interface GetTodayListeningDay {
    suspend operator fun invoke(chapter: ChapterLocationModel): ListeningDayModel?
}
